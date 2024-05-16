/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ParserTest1_7 extends AbstractCompilerTest {
	public static final boolean ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY = false;

static {
//	TESTS_NAMES = new String[] { "test0037"};
//	TESTS_RANGE = new int[] {10, 20};
//	TESTS_NUMBERS = new int[] { 10 };
}
public static Class testClass() {
	return ParserTest1_7.class;
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
public ParserTest1_7(String testName){
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
				true);
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
				true);

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
				true);
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
				true);

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
	{
		/* using regular parser in FULL mode */
		if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
			Parser parser =
				new Parser(
					new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						new CompilerOptions(getCompilerOptions()),
						new DefaultProblemFactory(Locale.getDefault())),
					true);
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
	}
	/* using regular parser in FULL mode and statementRecoveryEnabled */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				true);

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

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_7);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_7);
	return options;
}
public void test0001() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName")) {
					System.out.println(reader.read());
				} catch(FileNotFoundException | IOException | Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println("Finishing try-with-resources");
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException | IOException | Exception e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException | IOException | Exception e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException | IOException | Exception e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
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
public void test0002() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName")) {
					System.out.println(reader.read());
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					System.out.println("Finishing try-with-resources");
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
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
public void test0003() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName")) {
					System.out.println(reader.read());
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
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
public void test0004() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName")) {
					System.out.println(reader.read());
				} finally {
					System.out.println("Finishing try-with-resources");
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
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
public void test0005() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName")) {
					System.out.println(reader.read());
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
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
public void test0006() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try {
					System.out.println(reader.read());
				} catch(FileNotFoundException | IOException | Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println("Finishing try-with-resources");
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException | IOException | Exception e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException | IOException | Exception e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException | IOException | Exception e)
		      {
		        e.printStackTrace();
		      }
		    finally
		      {
		        System.out.println("Finishing try-with-resources");
		      }
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
public void test0007() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				List<String> l = new ArrayList<>();
				System.out.println(l);
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<String> l = new ArrayList<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<String> l = new ArrayList<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<String> l = new ArrayList<>();
		    System.out.println(l);
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
public void test0008() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				List<> l = new ArrayList<>();
				System.out.println(l);
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<> l = new ArrayList<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<> l = new ArrayList<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<> l = new ArrayList<>();
		    System.out.println(l);
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
public void test0009() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				List<String> l = new java.util.ArrayList<>();
				System.out.println(l);
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<String> l = new java.util.ArrayList<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<String> l = new java.util.ArrayList<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    List<String> l = new java.util.ArrayList<>();
		    System.out.println(l);
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
public void test0010() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				B<String>.C<Integer> o = new B<>.C<>();
				System.out.println(l);
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    B<String>.C<Integer> o = new B<>.C<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    B<String>.C<Integer> o = new B<>.C<>();
		    System.out.println(l);
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    B<String>.C<Integer> o = new B<>.C<>();
		    System.out.println(l);
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
public void test0011() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName");) {
					System.out.println(reader.read());
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
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
public void test0012() {

	String s =
		"""
		public class A {
			public void foo(String fileName) {
				try (Reader reader = new FileReader("fileName");
					Reader reader2 = new FileReader("fileName");) {
					System.out.println(reader.read());
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  public void foo(String fileName) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName");
		        Reader reader2 = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName");
		        Reader reader2 = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  public void foo(String fileName) {
		    try (Reader reader = new FileReader("fileName");
		        Reader reader2 = new FileReader("fileName"))
		      {
		        System.out.println(reader.read());
		      }
		    catch (FileNotFoundException e)
		      {
		        e.printStackTrace();
		      }
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
}
