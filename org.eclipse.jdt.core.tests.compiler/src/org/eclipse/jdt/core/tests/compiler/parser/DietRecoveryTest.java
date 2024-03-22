/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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

public class DietRecoveryTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$
static {
//	TESTS_NUMBERS = new int[] { 75 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(DietRecoveryTest.class);
}

public DietRecoveryTest(String testName){
	super(testName);
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
	/* using regular parser in DIET mode + getMethodBodies + statements recovery */
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
			"Invalid unit diet+body structure with statements recovery" + testName,
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
/*
 * Should treat variables 'h' and 'i' as fields since 'public'.
 */
public void test01() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String testName = "<promote local vars into fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out local type altogether
 */
public void test02() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L {							\t
					void baz(){}					\t
				}									\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class L {
		      L() {
		        super();
		      }
		      void baz() {
		      }
		    }
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String testName = "<filter out local type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still be finding last method (#baz)
 */

public void test03() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void baz(){							\t
			}										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String testName = "<should find last method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should finding 5 fields.
 */

public void test04() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
		 X x;									\t
		 Object a, b = null;						\t
			void foo() {							\t
				System.out.println();				\t
													\t
			public int h;							\t
			public int[] i = { 0, 1 };				\t
													\t
			void bar(){								\t
			void truc(){							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  X x;
		  Object a;
		  Object b = null;
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  X x;
		  Object a;
		  Object b = null;
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  X x;
		  Object a;
		  Object b;
		  public int h;
		  public int[] i;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String testName = "<five fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Diet parse thinks it is successful - no recovery
 */

public void test05() {

	String s =
		"""
		public class X {								\t
			void foo() {							\t
				System.out.println();				\t
		 	void baz(){}						\t
		 }										\t
													\t
			void bar(){								\t
		 }										\t
			void truc(){							\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"    new baz() {\n" +
		"    };\n"
		:
		"    void baz;\n"
		) +
		"  }\n" +
		"  void bar() {\n" +
		"  }\n" +
		"  void truc() {\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"""
		public class X {
		  {
		  }
		  public X() {
		  }
		  void foo() {
		  }
		  void baz() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<diet was successful>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Recovery will not restart from scratch, and miss some signatures (#baz())
 */

public void test06() {

	String s =
			"""
		import java.lang.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
		 	void baz(){}						\t
		 }										\t
													\t
			void bar(){								\t
		 }										\t
			void truc(){							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		import java.lang.*;
		public class X {
		  {
		  }
		  public X() {
		  }
		  void foo() {
		  }
		  void baz() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import java.lang.*;
		public class X {
		  {
		  }
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void baz() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<will not miss nested method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Properly attaching fields/methods to member type
 */

public void test08() {

	String s =
			"""
		public class X {							\t
		 class Y {								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    public int h;
		    public int[] i = {0, 1};
		    Y() {
		    }
		    void foo() {
		    }
		    void bar() {
		    }
		    void baz() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    public int h;
		    public int[] i = {0, 1};
		    Y() {
		      super();
		    }
		    void foo() {
		      System.out.println();
		    }
		    void bar() {
		    }
		    void baz() {
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  class Y {
		    public int h;
		    public int[] i;
		    Y() {
		    }
		    void foo() {
		    }
		    void bar() {
		    }
		    void baz() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String testName = "<attaching to member type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Properly attaching fields/methods to enclosing type
 */

public void test09() {

	String s =
			"""
		public class X {							\t
		 class Y {								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		    void foo() {
		      System.out.println();
		    }
		  }
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  public int h;
		  public int[] i;
		  public X() {
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String testName = "<attaching to enclosing type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Properly attaching fields/methods to member type in presence of missing
 * member type opening brace (Y) (and array initializer for (i)).
 */

public void test10() {

	String s =
			"""
		public class X {							\t
		 class Y 								\t
			  void foo() {							\t
			   System.out.println();				\t
		   }										\t
		 public int h;							\t
		 public int[] i = {0, 1};				\t
			void bar(){								\t
			void baz(){								\t
		 }										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    public int h;
		    public int[] i = {0, 1};
		    Y() {
		    }
		    void foo() {
		    }
		    void bar() {
		    }
		    void baz() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    public int h;
		    public int[] i = {0, 1};
		    Y() {
		      super();
		    }
		    void foo() {
		      System.out.println();
		    }
		    void bar() {
		    }
		    void baz() {
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  class Y {
		    public int h;
		    public int[] i;
		    Y() {
		    }
		    void foo() {
		    }
		    void bar() {
		    }
		    void baz() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String testName = "<missing brace + array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Attaching orphan methods and fields, by counting brackets
 * variable 'x' should be eliminated (looks like a local variable)
 */

public void test11() {

	String s =
			"""
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
		 }										\t
		}										\t
			void bar(){								\t
		  int x;									\t
			void baz(){								\t
		 }										\t
		 int y;									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  {
		  }
		  int y;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  {
		  }
		  int y;
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		    int x;
		  }
		  void baz() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<attaching orphans with missing brackets>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Attaching orphan methods and fields, by counting brackets
 * variable 'x' should NOT be eliminated given it looks like a field
 */

public void test12() {

	String s =
			"""
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
		 }										\t
		}										\t
			void bar(){								\t
		 public int x;							\t
			void baz(){								\t
		 }										\t
		 int y;									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  {
		  }
		  public int x;
		  int y;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  {
		  }
		  public int x;
		  int y;
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		  }
		  void baz() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<attaching orphans with missing brackets 2>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString		,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete type signature (missing superclass)
 */

public void test13() {

	String s =
			"""
		public class X extends {					\t
			void foo() {							\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<invalid type header>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete method signature (missing opening brace)
 */

public void test14() {

	String s =
			"""
		public class X extends Thread {			\t
			void foo() 								\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X extends Thread {
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X extends Thread {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<method header missing opening brace>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete method signature (missing thrown exceptions)
 */

public void test15() {

	String s =
			"""
		public class X extends Thread {			\t
			void foo() throws						\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X extends Thread {
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X extends Thread {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X extends Thread {
		  public X() {
		    super();
		  }
		  void foo() {
		    ;
		  }
		  void bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<method header missing thrown exceptions>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete type signature (missing superinterfaces)
 */

public void test16() {

	String s =
			"""
		public class X implements 					\t
			void foo() 								\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<type header missing superinterfaces>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should still recover incomplete type signature (missing superinterfaces)
 */

public void test17() {

	String s =
			"""
		public class X implements Y,				\t
			void foo() 								\t
			void bar() 								\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X implements Y {
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X implements Y {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<type header missing superinterfaces 2>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find member type behind incomplete enclosing type header
 */

public void test18() {

	String s =
			"""
		public class X implements 					\t
		 class Y { 								\t
			 void bar() 							\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		    void bar() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		    void bar() {
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<member type behind incomplete enclosing type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find member type when missing opening brace
 */

public void test19() {

	String s =
			"""
		public class X 		 					\t
		 class Y { 								\t
			 void bar() 							\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		    void bar() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		    void bar() {
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<member type when missing opening brace>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not find fieldX signature behind missing brace
 */

public void test20() {

	String s =
		"""
		public class X 		 					\t
		 fieldX;									\t
		 class Y { 								\t
			 void bar() 							\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		    void bar() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		    void bar() {
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<no field behind missing brace>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find Y as member type
 */

public void test21() {

	String s =
			"""
		public class X 		 					\t
		 fieldX;									\t
		 class Y  								\t
		 }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find Y as member type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete local type
 */

public void test22() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L extends {					\t
					public int l;					\t
					void baz(){}					\t
				}									\t
													\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class L {
		      public int l;
		      L() {
		        super();
		      }
		      void baz() {
		      }
		    }
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete local type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete local type and method signature
 */

public void test23() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				class L extends {					\t
					public int l;					\t
					void baz() throws {}			\t
				}									\t
													\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class L {
		      public int l;
		      L() {
		        super();
		      }
		      void baz() {
		      }
		    }
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete local type/method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out anonymous type
 */

public void test24() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() {}					\t
				}.baz();							\t
													\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new X() {
		  void baz() {
		  }
		}.baz();
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete anonymous type/method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete anonymous type
 */

public void test25() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() {}					\t
													\t
				public int h;						\t
													\t
				void bar(){							\t
				void truc(){						\t
		}\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
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
		import java.lang.*;
		import java.util.*;
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
		import java.lang.*;
		import java.util.*;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new X() {
		      public int h;
		      void baz() {
		      }
		      void bar() {
		      }
		      void truc() {
		      }
		    };
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete anonymous type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete anonymous method
 */

public void test26() {

	String s =
		"""
		package a;										\t
		import java.lang.*;						\t
		import java.util.*;						\t
													\t
		public class X {							\t
			void foo() {							\t
				System.out.println();				\t
													\t
				new X(){							\t
					void baz() 						\t
			    }									\t
			}										\t
			public int h;							\t
													\t
			void bar(){								\t
			void truc(){							\t
		}\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter out incomplete anonymous method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should filter out incomplete local type and local var h
 */

public void test27() {

	String s =
		"""
		package a;						\t
		import java.lang.*;			\t
		import java.util.*;			\t
										\t
		public class X {				\t
			void foo() {				\t
				System.out.println();	\t
										\t
				class L extends {		\t
					public int l;		\t
					void baz(){}		\t
				}						\t
										\t
				int h;					\t
										\t
			void bar(){					\t
			void truc(){				\t
		}								\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class L {
		      public int l;
		      L() {
		        super();
		      }
		      void baz() {
		      }
		    }
		    int h;
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should filter incomplete local type L and variable h>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find <y> as a field in Y
 */

public void test28() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  {			\t
		    int y;				\t
		}						\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    int y;
		    Y() {
		    }
		  }
		  int x;
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    int y;
		    Y() {
		      super();
		    }
		  }
		  int x;
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find <y> as a field in Y>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find <y> as a field in X
 */

public void test29() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  {			\t
		}						\t
		  int y;				\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		  }
		  int x;
		  int y;
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		  }
		  int x;
		  int y;
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find <y> as a field in X>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find <y> as a field in X
 */

public void test30() {

	String s =
		"""
		public class X {		 \t
		  int x;			 	\t
								\t
		  int foo(){ }			\t
								\t
		  class Y  			\t
		}						\t
		  int y;				\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		    }
		  }
		  int x;
		  int y;
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		  }
		  int x;
		  int y;
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find <y> as a field in X>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should recover from partial method header foo()
 */

public void test31() {

	String s =
		"""
		package a;							\t
		import java.lang.*;				\t
		import java.util.*;				\t
											\t
		public class X {					\t
			void foo() 						\t
				System.out.println();		\t
											\t
			public int h;					\t
			public int[] i = { 0, 1 };		\t
											\t
			void bar(){						\t
			void truc(){					\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i = {0, 1};
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		import java.lang.*;
		import java.util.*;
		public class X {
		  public int h;
		  public int[] i;
		  public X() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		  void truc() {
		  }
		}
		""";

	String testName = "<should recover from partial method header>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should recover from method with missing argument names
 */

public void _test32() {

	String s =
		"""
		public class WB2 {										\t
			public void foo(java.util.Locale, java.util.Vector) {\t
				int i;											\t
				if(i instanceof O) {							\t
				}												\t
				String s = "hello";							\t
				s.												\t
			}													\t
		}														\t
		""";

	String expectedDietUnitToString =
		"""
		public class WB2 {
		  public WB2() {
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class WB2 {
		  public WB2() {
		    super();
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"""
				public class WB2 {
				  public WB2() {
				    super();
				  }
				  public void foo() {
				    java.util.Locale.java.util.Vector $missing$;
				  }
				}
				""";
	} else {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			expectedDietPlusBodyUnitToString;
	}

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should recover from method with missing argument names>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not find message with no argument as a constructor
 */

public void test33() {

	String s =
		"""
		public class X {			\t
			void hello()			\t
			public X(int i)			\t
			void foo() {			\t
				System.out.println();\t
									\t
		}							\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  void hello() {
		  }
		  public X(int i) {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  void hello() {
		  }
		  public X(int i) {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not find message with no argument as a constructor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not find allocation as a constructor
 */

public void test34() {

	String s =
		"""
		public class X {			\t
			void hello()			\t
			public X(int i)			\t
			static void foo() {		\t
				X x;				\t
				x = new X(23);		\t
				System.out.println();\t
									\t
		}							\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  void hello() {
		  }
		  public X(int i) {
		  }
		  static void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  void hello() {
		  }
		  public X(int i) {
		    super();
		  }
		  static void foo() {
		    X x;
		    x = new X(23);
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not find allocation as a constructor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Incomplete field header
 */

public void test35() {

	String s =
		"public class X {		\n" +
		"	int x				\n";

	String expectedDietUnitToString =
		"""
		public class X {
		  int x;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int x;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<incomplete field header>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Incomplete multiple field headers
 */

public void test36() {

	String s =
		"public class X {		\n" +
		"	int x, y			\n";

	String expectedDietUnitToString =
		"""
		public class X {
		  int x;
		  int y;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int x;
		  int y;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<incomplete multiple field headers>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field header with started string initializer
 */

public void test37() {

	String s =
		"public class X {		\n" +
		"	String s = \"		\n";

	String expectedDietUnitToString =
		"""
		public class X {
		  String s;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  String s;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field header with started string initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field header with started string initializer combined with incomplete superinterface
 */

public void test38() {

	String s =
		"public class X implements Y, {		\n" +
		"	String s = \"					\n";

	String expectedDietUnitToString =
		"""
		public class X implements Y {
		  String s;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X implements Y {
		  String s;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field header and incomplete superinterface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field signature behind keyword implements
 */

public void test39() {

	String s =
		"""
		public class X implements 	\t
		int x						\t
		}							\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  int x;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int x;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field signature behind keyword implements>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Field type read as interface
 */

public void test40() {

	String s =
		"public class X implements Y, 		\n" +
		"	String s = \"					\n";

	String expectedDietUnitToString =
		"""
		public class X implements Y, String {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X implements Y, String {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<field type read as interface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Contiguous headers (checking checkpoint positions)
 */

public void test41() {

	String s =
		"public class X public int foo(int bar(static String s";

	String expectedDietUnitToString =
		"""
		public class X {
		  static String s;
		  public X() {
		  }
		  <clinit>() {
		  }
		  public int foo() {
		  }
		  int bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  static String s;
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		  public int foo() {
		  }
		  int bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<contiguous headers>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Contiguous headers without comma (checking checkpoint positions)
 */

public void test42() {

	String s =
		"public class X public int foo(int x, int bar public String s;";

	String expectedDietUnitToString =
		"""
		public class X {
		  public String s;
		  public X() {
		  }
		  public int foo(int x, int bar) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public String s;
		  public X() {
		    super();
		  }
		  public int foo(int x, int bar) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<contiguous headers without comma>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Contiguous headers without comma (checking checkpoint positions)
 */

public void test43() {

	String s =
		"""
		public class X 		\t
			public int foo(		\t
			int bar(			\t
		 	static String s, int x\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  static String s;
		  int x;
		  public X() {
		  }
		  <clinit>() {
		  }
		  public int foo() {
		  }
		  int bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  static String s;
		  int x;
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		  public int foo() {
		  }
		  int bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<contiguous headers without comma>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find static field <x>
 */

public void test44() {

	String s =
		"""
		class X {				\t
			String s;			\t
								\t
			public void foo(	\t
				static int x	\t
		}						\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  String s;
		  static int x;
		  X() {
		  }
		  <clinit>() {
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  String s;
		  static int x;
		  X() {
		    super();
		  }
		  <clinit>() {
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find static field x>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Missing string literal quote inside method
 */

public void test45() {

	String s =
		"""
		public class X {		\t
			int foo(){			\t
				String s = "	\t
			}					\t
		}						\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<missing string literal quote inside method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Detecting member type closing when missing brackets
 */

public void test46() {

	String s =
		"""
		class X 				\t
		  String s = "class y \t
		  class Member 		\t
			int foo() 			\t
		        public int x;    \t
		  } 					\t
		 int bar() 			\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  class Member {
		    public int x;
		    Member() {
		    }
		    int foo() {
		    }
		  }
		  String s;
		  X() {
		  }
		  int bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  class Member {
		    public int x;
		    Member() {
		      super();
		    }
		    int foo() {
		    }
		  }
		  String s;
		  X() {
		    super();
		  }
		  int bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<detecting member type closing when missing brackets>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated method arguments
 */

public void test47() {

	String s =

		"""
		class X {								\t
			int foo(AA a, BB b, IOEx			\t
												\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  X() {
		  }
		  int foo(AA a, BB b) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int foo(AA a, BB b) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated method arguments>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated literal string in method body
 */

public void test48() {

	String s =
		"""
		public class X {						\t
			final static int foo(){ 			\t
				return "1; 					\t
			} 									\t
			public static void main(String argv[]){\s
				foo();							\t
			} 									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  static final int foo() {
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";
	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  static final int foo() {
		  }
		  public static void main(String[] argv) {
		    foo();
		  }
		}
		""";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated literal string in method body>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated initializer with local declaration
 */

public void test49() {

	String s =
		"""
		public class X {						\t
			{									\t
		     int x;							\t
			 									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated initializer with local declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated if statement
 */

public void test50() {

	String s =
		"""
		public class X {						\t
		   int foo(){							\t
			  if(true){							\t
		     	int x;							\t
			 									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  int foo() {
		    if (true)
		        {
		          int x;
		        }
		    else
		        ;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated if statement>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated nested block with local declaration
 */

public void test51() {

	String s =
		"""
		public class X {						\t
		   int foo(){							\t
			  {									\t
		     	int x;							\t
			 									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  int foo() {
		    {
		      int x;
		    }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated nested block with local declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated nested block with field declaration
 */

public void test52() {

	String s =
		"""
		public class X {						\t
		   int foo(){							\t
			  {									\t
		     	public int x;					\t
			 									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public int x;
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public int x;
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated nested block with field declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated initializer with field declaration
 */

public void test53() {

	String s =
		"""
		public class X {						\t
			{									\t
		     public int x;						\t
			 									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  {
		  }
		  public int x;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  {
		  }
		  public int x;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated initializer with field declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Invalid class name
 */

public void test54() {

	String s =
		"""
		package p;							\t
		public class ZPro.Sev.Blo {													\t
		void ThisIsADummyMethodThatIsCreatedOnlyForThePurposesOfTheCompletionEngine() {\t
			System.out.println(this.getClass());										\t
		}																				\t
			// COMMENT																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package p;
		public class ZPro {
		  {
		  }
		  public ZPro() {
		  }
		  void ThisIsADummyMethodThatIsCreatedOnlyForThePurposesOfTheCompletionEngine() {
		  }
		}
		""";
	String expectedDietPlusBodyUnitToString =
		"""
		package p;
		public class ZPro {
		  {
		  }
		  public ZPro() {
		    super();
		  }
		  void ThisIsADummyMethodThatIsCreatedOnlyForThePurposesOfTheCompletionEngine() {
		    System.out.println(this.getClass());
		  }
		}
		""";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<Invalid class name>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated static initializer with field declaration
 */

public void test55() {

	String s =
		"""
		public class X {						\t
			static {							\t
		     public int x;						\t
			 									\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  static {
		  }
		  public int x;
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  static {
		  }
		  public int x;
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unterminated static initializer with field declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Multiple initializers combined with array initializer
 */

public void test56() {

	String s =
		"""
		public class X 			\t
			static int zz			\t
			{						\t
			}						\t
			static {				\t
		   public int x;			\t
			int[] y = { 0, 1};		\t
			{						\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  static int zz;
		  {
		  }
		  static {
		  }
		  public int x;
		  int[] y = {0, 1};
		  {
		  }
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  static int zz;
		  {
		  }
		  static {
		  }
		  public int x;
		  int[] y = {0, 1};
		  {
		  }
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  static int zz;
		  {
		  }
		  static {
		  }
		  public int x;
		  int[] y;
		  {
		  }
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String testName = "<multiple initializers combined with array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Combination of unterminated methods and fields
 */

public void test57() {

	String s =
		"""
		class X					\t
			void foo(){				\t
				{					\t
			public static int x;	\t
			void bar()				\t
			}						\t
			int y;					\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  public static int x;
		  int y;
		  X() {
		  }
		  <clinit>() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  public static int x;
		  int y;
		  X() {
		    super();
		  }
		  <clinit>() {
		  }
		  void foo() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<combination of unterminated methods and fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Illegal unicode inside method body
 */

public void test58() {

	String s =
		"""
		package p;\s
														\t
		class A {										\t
			void bar() {								\t
				String s = "\\u000D";					\t
			}											\t
		}												\t
		""";

	String expectedDietUnitToString =
		"""
		package p;
		class A {
		  A() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package p;
		class A {
		  A() {
		    super();
		  }
		  void bar() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<illegal unicode inside method body>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Extra identifier in type signature
 */

public void test59() {

	String s =
		"public class X extends java.io.IOException IOException  {			\n" +
		"}																	\n";

	String expectedDietUnitToString =
		"""
		public class X extends java.io.IOException {
		  {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X extends java.io.IOException {
		  {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<extra identifier in type signature>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Extra identifier in method signature
 */

public void test60() {

	String s =
		"""
		public class X extends java.io.IOException  {	\t
			int foo() ExtraIdentifier {					\t
		}												\t
		""";

	String expectedDietUnitToString =
		"""
		public class X extends java.io.IOException {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X extends java.io.IOException {
		  public X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<extra identifier in method signature>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Extra identifier behind thrown exception
 */

public void test61() {

	String s =
		"""
		public class X extends  {						\t
			int foo() throws IOException ExtraIdentifier {\t
		}												\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() throws IOException {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  int foo() throws IOException {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<extra identifier behind thrown exception>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unterminated array initializer
 */

public void test62() {

	String s =
		"""
		class X {			\t
		 class Y 			\t
		   public String s;\t
		   int foo(){		\t
			return 1;		\t
		   static int y = {;\t
		 }					\t
		 public int i = 0;	\t
		 					\t
		 int baz()			\t
							\t
		}					\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  class Y {
		    public String s;
		    static int y;
		    public int i = 0;
		    Y() {
		    }
		    <clinit>() {
		    }
		    int foo() {
		    }
		    int baz() {
		    }
		  }
		  X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  class Y {
		    public String s;
		    static int y;
		    public int i = 0;
		    Y() {
		      super();
		    }
		    <clinit>() {
		    }
		    int foo() {
		      return 1;
		    }
		    int baz() {
		    }
		  }
		  X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		class X {
		  class Y {
		    public String s;
		    static int y;
		    public int i;
		    Y() {
		    }
		    <clinit>() {
		    }
		    int foo() {
		    }
		    int baz() {
		    }
		  }
		  X() {
		  }
		}
		""";

	String testName = "<unterminated array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Initializer behind array initializer
 */

public void test63() {

	String s =
		"""
		class X {			\t
		 int x[] = {0, 1}	\t
		 {					\t
		 }					\t
		}					\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  int[] x = {0, 1};
		  {
		  }
		  X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  int[] x = {0, 1};
		  {
		  }
		  X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		class X {
		  int[] x;
		  {
		  }
		  X() {
		  }
		}
		""";

	String testName = "<initializer behind array initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Initializers mixed with fields
 */

public void test64() {

	String s =
		"""
		public class X 		\t
			int[] x = { 0, 1};	\t
			static int zz		\t
			{					\t
			}					\t
			static {			\t
		    public int x;		\t
			{					\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  int[] x = {0, 1};
		  static int zz;
		  {
		  }
		  static {
		  }
		  public int x;
		  {
		  }
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int[] x = {0, 1};
		  static int zz;
		  {
		  }
		  static {
		  }
		  public int x;
		  {
		  }
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  int[] x;
		  static int zz;
		  {
		  }
		  static {
		  }
		  public int x;
		  {
		  }
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String testName = "<initializers mixed with fields>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find method behind some()
 */

public void test65() {

	String s =
		"""
		import java.lang.*;												\t
																			\t
		public class Hanoi {												\t
		private    Post[] posts;											\t
		public static void main (String args[]) {							\t
		}																	\t
		public void some(){												\t
																			\t
		private void moveDisk (Post source, Post destination) {			\t
		}																	\t
		protected void reportMove (Post source, Post destination) {		\t
		}																	\t
		private void reset () {											\t
		}																	\t
		public void solve () {												\t
		}																	\t
		private void solve (int depth, Post start, Post free, Post end) {	\t
		}																	\t
		}																	\t
		""";

	String expectedDietUnitToString =
		"""
		import java.lang.*;
		public class Hanoi {
		  private Post[] posts;
		  public Hanoi() {
		  }
		  public static void main(String[] args) {
		  }
		  public void some() {
		  }
		  private void moveDisk(Post source, Post destination) {
		  }
		  protected void reportMove(Post source, Post destination) {
		  }
		  private void reset() {
		  }
		  public void solve() {
		  }
		  private void solve(int depth, Post start, Post free, Post end) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import java.lang.*;
		public class Hanoi {
		  private Post[] posts;
		  public Hanoi() {
		    super();
		  }
		  public static void main(String[] args) {
		  }
		  public void some() {
		  }
		  private void moveDisk(Post source, Post destination) {
		  }
		  protected void reportMove(Post source, Post destination) {
		  }
		  private void reset() {
		  }
		  public void solve() {
		  }
		  private void solve(int depth, Post start, Post free, Post end) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find method behind some()>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should detect X(int) as a method with no return type
 */

public void test66() {

	String s =
		"""
		class X {		\t
			class Y {	\t
			X(int i){}	\t
		}				\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  class Y {
		    Y() {
		    }
		    X(int i) {
		    }
		  }
		  X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  class Y {
		    Y() {
		      super();
		    }
		    X(int i) {
		    }
		  }
		  X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should detect X(int) as a method with no return type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should detect orphan X(int) as a constructor
 */

public void test67() {

	String s =
		"""
		class X {		\t
			class Y {	\t
			}			\t
		}				\t
			X(int i){	\t
		   }			\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  class Y {
		    Y() {
		    }
		  }
		  {
		  }
		  X(int i) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  class Y {
		    Y() {
		      super();
		    }
		  }
		  {
		  }
		  X(int i) {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should detect orphan X(int) as a constructor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Empty unit
 */

public void test68() {

	String s = "";

	String expectedDietUnitToString = "";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<empty unit>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Unit reduced to a method declaration
 */

/*
 * Unit reduced to a constructor declaration
 */

public void test70() {

	String s =
		"""
			X(){					\t
				System.out.println();\t
			}						\t
		""";

	String expectedDietUnitToString = "";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<unit reduced to a constructor declaration>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not pick-up any constructor with no arg
 */

public void test73() {

	String s =
		"""
			class X {		\t
				X(int i){}	\t
				int foo(){	\t
					new X(	\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  X(int i) {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  X(int i) {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		class X {
		  X(int i) {
		    super();
		  }
		  int foo() {
		    new X();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not pick-up any constructor with no arg>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect any field
 */

public void test74() {

	String s =
		"""
		package pack;					\t
										\t
		class A extends IOException {	\t
										\t
			S{							\t
				int x;					\t
			}							\t
		}								\t
		""";

	String expectedDietUnitToString =
		"""
		package pack;
		class A extends IOException {
		  {
		  }
		  A() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package pack;
		class A extends IOException {
		  {
		    int x;
		  }
		  A() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect any field>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Bunch of syntax errors
 */

public void test75() {

	String s =
		"""
		package ZKentTest;
		
		import java.awt.color.*;
		
		public class A {
			A foo(int i) { return this; }
			int[] ii = {0, 1clone()
		
			int bar() {
				class Local {
					int hello(){
						fo
					}
					int world()
					}
			void foo() {
				ba	\t
		""";

	String expectedDietUnitToString =
		"""
		package ZKentTest;
		import java.awt.color.*;
		public class A {
		  int[] ii;
		  public A() {
		  }
		  A foo(int i) {
		  }
		  int bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package ZKentTest;
		import java.awt.color.*;
		public class A {
		  int[] ii;
		  public A() {
		    super();
		  }
		  A foo(int i) {
		    return this;
		  }
		  int bar() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package ZKentTest;
		import java.awt.color.*;
		public class A {
		  int[] ii;
		  public A() {
		    super();
		  }
		  A foo(int i) {
		    return this;
		  }
		  int bar() {
		    class Local {
		      Local() {
		        super();
		      }
		      int hello() {
		        fo = $missing$;
		      }
		      int world() {
		      }
		      void foo() {
		      }
		    }
		    ba = $missing$;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package ZKentTest;
		import java.awt.color.*;
		public class A {
		  int[] ii;
		  public A() {
		  }
		  A foo(int i) {
		  }
		}
		class Local {
		  Local() {
		  }
		  int hello() {
		  }
		  int world() {
		  }
		  void foo() {
		  }
		}
		""";

	String testName = "<bunch of syntax errors>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find Member as a member type
 */

public void test76() {

	String s =
		"""
		package pack;							\t
		class A  {								\t
												\t
			public static void main(String[] argv)\t
					new Member().f				\t
					;							\t
			}									\t
			class Member {						\t
				int foo()						\t
				}								\t
			}									\t
		};										\t
		""";

	String expectedDietUnitToString =
		"""
		package pack;
		class A {
		  class Member {
		    Member() {
		    }
		    int foo() {
		    }
		  }
		  A() {
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package pack;
		class A {
		  class Member {
		    Member() {
		      super();
		    }
		    int foo() {
		    }
		  }
		  A() {
		    super();
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package pack;
		class A {
		  class Member {
		    Member() {
		      super();
		    }
		    int foo() {
		    }
		  }
		  A() {
		    super();
		  }
		  public static void main(String[] argv) {
		    new Member().f = $missing$;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find Member as a member type>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not recover duplicate field numberOfDisks
 */

public void test77() {

	String s =
		"""
		package p;														\t
																			\t
		import java.lang.*;												\t
																			\t
		class IncompleteHanoi {											\t
		private    Post[] posts;											\t
		private    int numberOfDisks;										\t
																			\t
		public Hanoi (int numberOfDisks) {									\t
		 this.numberOfDisks = numberOfDisks;								\t
		'' this.posts = new Post[3];										\t
		 String[] postNames = new String[]{"Left", "Middle", "Right"};\t
																			\t
		 for (int i = 0; i < 3; ++i)										\t
		  this.posts[i] = new Post(postNames[i], numberOfDisks);			\t
		}																	\t
																			\t
		private void solve (int depth, Post start, Post free, Post end) {	\t
		 if (depth == 1)													\t
		  moveDisk(start, end);											\t
		 else if (depth > 1) {												\t
		  sol																\t
		""";

	String expectedDietUnitToString =
		"""
		package p;
		import java.lang.*;
		class IncompleteHanoi {
		  private Post[] posts;
		  private int numberOfDisks;
		  IncompleteHanoi() {
		  }
		  public Hanoi(int numberOfDisks) {
		  }
		  private void solve(int depth, Post start, Post free, Post end) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package p;
		import java.lang.*;
		class IncompleteHanoi {
		  private Post[] posts;
		  private int numberOfDisks;
		  IncompleteHanoi() {
		    super();
		  }
		  public Hanoi(int numberOfDisks) {
		  }
		  private void solve(int depth, Post start, Post free, Post end) {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package p;
		import java.lang.*;
		class IncompleteHanoi {
		  private Post[] posts;
		  private int numberOfDisks;
		  IncompleteHanoi() {
		    super();
		  }
		  public Hanoi(int numberOfDisks) {
		  }
		  private void solve(int depth, Post start, Post free, Post end) {
		    if ((depth == 1))
		        moveDisk(start, end);
		    else
		        if ((depth > 1))
		            {
		              sol = $missing$;
		            }
		        else
		            ;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not recover duplicate field numberOfDisks>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect a field v (1/2)
 */

public void test78() {

	String s =
		"""
		class X {							\t
			int foo(){						\t
				Vector v = new Vector();	\t
				s							\t
				v.addElement(				\t
			}								\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int foo() {
		    Vector v = new Vector();
		    s v;
		    addElement();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect a field v>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect a field v (2/2)
 */

public void test79() {

	String s =
		"""
		class X {							\t
			int foo(){						\t
				Vector v = new Vector();	\t
				public s   v.addElement(	\t
			}								\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int foo() {
		    Vector v = new Vector();
		    public s v;
		    addElement();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect a field v>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not detect a method bar
 */

public void test80() {

	String s =
		"""
		class X {							\t
			int test(){						\t
				int[] i;					\t
				i							\t
				// some comment				\t
				bar(1);						\t
			}								\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  X() {
		  }
		  int test() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int test() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		class X {
		  X() {
		    super();
		  }
		  int test() {
		    int[] i;
		    i bar = 1;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not detect a method bar>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not pick-up any constructor with no arg
 */

public void test81() {

	String s =
		"""
			class X {			\t
				X(int i){}		\t
				int foo(){		\t
					X(12)		\t
		""";

	String expectedDietUnitToString =
		"""
		class X {
		  X(int i) {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  X(int i) {
		    super();
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		class X {
		  X(int i) {
		    super();
		  }
		  int foo() {
		    X(12);
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not pick-up any constructor with no arg>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not promote message sending as a method
 */

public void test82() {

	String s =
		"""
		public class A {	\t
							\t
			void foo() 		\t
				if (true) {	\t
				} else {	\t
					Bar s; 	\t
					s.fred();\t
				}			\t
			}				\t
		}					\t
		""";
	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  void foo() {
		    if (true)
		        {
		        }
		    else
		        {
		          Bar s;
		          s.fred();
		        }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not promote message sending as a method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should not promote message sending as a method 2
 */

public void test83() {

	String s =
		"""
		public class A {		\t
								\t
			void foo() if (true) {\t
				} else {		\t
					Bar s; 		\t
					s.fred();	\t
				}				\t
			}					\t
		}						\t
		""";
	String expectedDietUnitToString =
		"""
		public class A {
		  public A() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  public A() {
		    super();
		  }
		  void foo() {
		    if (true)
		        {
		        }
		    else
		        {
		          Bar s;
		          s.fred();
		        }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should not promote message sending as a method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find a static initializer
 */

public void test84() {

	String s =
		"""
		public class A extends		\t
									\t
			static {				\t
		}							\t
		""";
	String expectedDietUnitToString =
		"""
		public class A {
		  static {
		  }
		  public A() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  static {
		  }
		  public A() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find a static initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find a static initializer
 */

public void test85() {

	String s =
		"""
		public class A 		\t
								\t
			static {			\t
		}						\t
		""";
	String expectedDietUnitToString =
		"""
		public class A {
		  static {
		  }
		  public A() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  static {
		  }
		  public A() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find a static initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find an initializer
 */

public void test86() {

	String s =
		"""
		public class A 		\t
								\t
			int 				\t
			{					\t
		}						\t
		""";
	String expectedDietUnitToString =
		"""
		public class A {
		  {
		  }
		  public A() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  {
		  }
		  public A() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find an initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Should find an initializer
 */

public void test87() {

	String s =
		"""
		public class A 		\t
								\t
			int x;				\t
		  {					\t
			int y;				\t
		}						\t
		""";
	String expectedDietUnitToString =
		"""
		public class A {
		  int x;
		  {
		  }
		  public A() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class A {
		  int x;
		  {
		    int y;
		  }
		  public A() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<should find an initializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVRQG0: ITPCOM:WINNT - NullPointerException in recovery mode
 */

public void test88() {

	String s =
		"""
		package p1;				\t
									\t
		public class X {			\t
			int foo(String s, int x) \t
			public int y = new X() { \t
									\t
		}							\t
		""";

	String expectedDietUnitToString =
		"""
		package p1;
		public class X {
		  public int y;
		  public X() {
		  }
		  int foo(String s, int x) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package p1;
		public class X {
		  public int y;
		  public X() {
		    super();
		  }
		  int foo(String s, int x) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package p1;
		public class X {
		  public int y;
		  public X() {
		  }
		  int foo(String s, int x) {
		  }
		}
		""";

	String testName = "<1FVRQG0: ITPCOM:WINNT - NullPointerException in recovery mode>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVRN9V: ITPJCORE:WIN98 - Internal builder error compiling servlet
 */

public void test89() {

	String s =
		"""
		import javax.servlet.*;										\t
		import javax.servlet.http.*;									\t
																		\t
		public class Servlet1 extends HttpServlet {					\t
			protected (HttpServletRequest req, HttpServletResponse resp) {\t
			}															\t
		}																\t
		""";

	String expectedDietUnitToString =
		"""
		import javax.servlet.*;
		import javax.servlet.http.*;
		public class Servlet1 extends HttpServlet {
		  HttpServletRequest req;
		  HttpServletRequest HttpServletResponse;
		  {
		  }
		  public Servlet1() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import javax.servlet.*;
		import javax.servlet.http.*;
		public class Servlet1 extends HttpServlet {
		  HttpServletRequest req;
		  HttpServletRequest HttpServletResponse;
		  {
		  }
		  public Servlet1() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVRN9V: ITPJCORE:WIN98 - Internal builder error compiling servlet>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVXQZ4: ITPCOM:WIN98 - Walkback during parsing recovery
 */

public void test90() {

	String s =
		"""
		public class Test {\t
							\t
			int x;			\t
			int foo(		\t
			int bar(		\t
			baz(A a			\t
		}					\t
		""";
	String expectedDietUnitToString =
		"""
		public class Test {
		  int x;
		  public Test() {
		  }
		  int foo() {
		  }
		  int bar() {
		  }
		  baz(A a) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class Test {
		  int x;
		  public Test() {
		    super();
		  }
		  int foo() {
		  }
		  int bar() {
		  }
		  baz(A a) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVXQZ4: ITPCOM:WIN98 - Walkback during parsing recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface
 */

public void test91() {

	String s =
		"""
		public interface Fred {	\t
			void foo();				\t
			void bar();				\t
			public fred(X x, int y);\t
		}							\t
		""";
	String expectedDietUnitToString =
		"""
		public interface Fred {
		  void foo();
		  void bar();
		  public fred(X x, int y);
		}
		""";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * Variation on 1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface
 */

public void test92() {
	String s =
		"""
		public interface Test {	\t
			void foo();				\t
									\t
			public fred(Fred x, int y);\t
		}							\t
		""";
	String expectedDietUnitToString =
		"""
		public interface Test {
		  void foo();
		  public fred(Fred x, int y);
		}
		""";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FVXWKI: ITPCOM:WIN98 - Walkback when parsing a bogus interface>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FW5A4E: ITPCOM:WIN98 - Walkback reconciling
 */

public void test93() {
	String s =
		"""
		class X{		\t
			int foo()	\t
			static { }	\t
		}				\t
		""";
	String expectedDietUnitToString =
		"""
		class X {
		  static {
		  }
		  X() {
		  }
		  <clinit>() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  static {
		  }
		  X() {
		    super();
		  }
		  <clinit>() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FW5A4E: ITPCOM:WIN98 - Walkback reconciling>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FW3663: ITPCOM:WIN98 - Outline - does not show method #fred()
 */

public void test94() {
	String s =
		"""
		public class X {					\t
			int[] array;					\t
											\t
		void foo() {						\t
			bar(this.array.length, 10, fred(\t
											\t
		int fred(							\t
		}									\t
		""";
	String expectedDietUnitToString =
		"""
		public class X {
		  int[] array;
		  public X() {
		  }
		  void foo() {
		  }
		  int fred() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int[] array;
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		  int fred() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X {
		  int[] array;
		  public X() {
		    super();
		  }
		  void foo() {
		    bar(this.array.length, 10, fred());
		  }
		  int fred() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FW3663: ITPCOM:WIN98 - Outline - does not show method #fred()>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FW6M5M: ITPJUI:ALL - NPE in SourceElementParser
 */

public void test95() {
	String s =
		"""
		public interface IP {		\t
			public static toString() {\t
			}						\t
		}							\t
		""";
	String expectedDietUnitToString =
		"""
		public interface IP {
		  public static toString() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString = expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FW6M5M: ITPJUI:ALL - NPE in SourceElementParser>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * 1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import
 */

public void test96() {
	String s =
		"""
		import ;
		class X {
			int foo(){
				System.out.println();
			}
			static {
				int i = j;
			}
		}
		""";
	String expectedDietUnitToString =
		"""
		class X {
		  static {
		  }
		  X() {
		  }
		  <clinit>() {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  static {
		    int i = j;
		  }
		  X() {
		    super();
		  }
		  <clinit>() {
		  }
		  int foo() {
		    System.out.println();
		  }
		}
		""";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * variation on 1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import
 */

public void test97() {
	String s =
		"""
		import ;
		class X {
			int foo(){
				System.out.println();
			}
			static {
			}
		}
		""";
	String expectedDietUnitToString =
		"""
		class X {
		  static {
		  }
		  X() {
		  }
		  <clinit>() {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  static {
		  }
		  X() {
		    super();
		  }
		  <clinit>() {
		  }
		  int foo() {
		    System.out.println();
		  }
		}
		""";
	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<variation on 1FWHXX7: ITPCOM:WINNT - ClassCastException compiling invalid import>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=9084
 */

public void test98() {

	String s =
		"""
		public class A {		                                               \s
			class Platform {		                                           \s
				public static void run(Runnable r) {		                   \s
				}		                                                       \s
			}			                                                       \s
			Object [] array = null;		                                       \s
			for (int nX = 0; nX < array.length; nX ++) {		               \s
				final String part = "";		                               \s
				final String sel = "";		                               \s
				Object l = null;		                                       \s
				if ((part != null && sel != null) || l instanceof String) {\t
					Platform.run(new Runnable() {		                       \s
						public void run() {		                               \s
						}		                                               \s
						public void handleException(Throwable e) {		       \s
						}		                                               \s
					});		                                                   \s
				}		                                                       \s
			}		                                                           \s
		}                                                                     \s
		""";

	String expectedDietUnitToString =
		"""
		public class A {
		  class Platform {
		    Platform() {
		    }
		    public static void run(Runnable r) {
		    }
		  }
		  Object[] array = null;
		  int nX = 0;
		  {
		  }
		  public A() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString = """
		public class A {
		  class Platform {
		    Platform() {
		      super();
		    }
		    public static void run(Runnable r) {
		    }
		  }
		  Object[] array = null;
		  int nX = 0;
		  {
		    final String part = "";
		    final String sel = "";
		    Object l = null;
		    if ((((part != null) && (sel != null)) || (l instanceof String)))
		        {
		          Platform.run(new Runnable() {
		  public void run() {
		  }
		  public void handleException(Throwable e) {
		  }
		});
		        }
		  }
		  public A() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class A {
		  class Platform {
		    Platform() {
		    }
		    public static void run(Runnable r) {
		    }
		  }
		  Object[] array;
		  int nX;
		  {
		  }
		  public A() {
		  }
		}
		""";

	String testName = "<check for null inside RecoveredInitializer>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}

public void test99() {
	String s =
		"""
		import ;
		class X {
		}
		- public void addThreadFilter(IJavaThread thread) - restricts breakpoint to\s
		given thread and any other previously specified threads
		- public void removeThreadFilter(IJavaThread thread)- removes the given thread\s
		restriction (will need to re-create breakpoint request as JDI does not support\s
		the removal of thread filters)
		- public IJavaThread[] getThreadFilters() - return the set of threads this\s
		breakpoint is currently restricted to
		""";
	String expectedDietUnitToString =
		"""
		class X {
		  {
		  }
		  X() {
		  }
		  public void addThreadFilter(IJavaThread thread) {
		  }
		  public void removeThreadFilter(IJavaThread thread) {
		  }
		  public IJavaThread[] getThreadFilters() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		class X {
		  {
		  }
		  X() {
		    super();
		  }
		  public void addThreadFilter(IJavaThread thread) {
		  }
		  public void removeThreadFilter(IJavaThread thread) {
		  }
		  public IJavaThread[] getThreadFilters() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		class X {
		  {
		  }
		  X() {
		    super();
		  }
		  public void addThreadFilter(IJavaThread thread) {
		    restricts breakpoint;
		    given thread;
		    any other;
		    specified = $missing$;
		  }
		  public void removeThreadFilter(IJavaThread thread) {
		    removes the;
		    thread restriction;
		    will need = (re - create);
		    request as;
		    does not;
		    the removal;
		    thread = $missing$;
		  }
		  public IJavaThread[] getThreadFilters() {
		    return the;
		    of threads;
		    breakpoint is;
		    restricted to;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<9101 - Parse error while typing in Java editor>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test100() {
	String s =
		"""
		public class Bug {
			static boolean bold = false;
		public static void main(String arguments[]) {
			Shell shell = new Shell(SWT.MENU | SWT.RESIZE | SWT.TITLE | SWT.H_SCROLL);
			StyledText text = new StyledText(shell, SWT.WRAP);\s
			shell.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event e) {
					text.setBounds(shell.getClientArea());			\s
				} \s
			});\t
			shell.addListener(SWT.KeyDown, bew Listener () {
				public void handleEvent(Event e) {
					bold = !bold;
				}
			});\s
			text.addLineStyleListener(new LineStyleListener() {\s
				public void lineGetStyle(LineStyleEvent event) {
				}
			});
		}
		}
		""";

	String expectedDietUnitToString =
		"""
		public class Bug {
		  static boolean bold = false;
		  <clinit>() {
		  }
		  public Bug() {
		  }
		  public static void main(String[] arguments) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class Bug {
		  static boolean bold = false;
		  <clinit>() {
		  }
		  public Bug() {
		    super();
		  }
		  public static void main(String[] arguments) {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class Bug {
		  static boolean bold = false;
		  <clinit>() {
		  }
		  public Bug() {
		    super();
		  }
		  public static void main(String[] arguments) {
		    Shell shell = new Shell((((SWT.MENU | SWT.RESIZE) | SWT.TITLE) | SWT.H_SCROLL));
		    StyledText text = new StyledText(shell, SWT.WRAP);
		    shell.addListener(SWT.Resize, new Listener() {
		  public void handleEvent(Event e) {
		    text.setBounds(shell.getClientArea());
		  }
		});
		    shell.addListener(SWT.KeyDown, new Listener() {
		  public void handleEvent(Event e) {
		    bold = (! bold);
		  }
		});
		    text.addLineStyleListener(new LineStyleListener() {
		  public void lineGetStyle(LineStyleEvent event) {
		  }
		});
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		public class Bug {
		  static boolean bold = false;
		  public Bug() {
		  }
		  <clinit>() {
		  }
		  public static void main(String[] arguments) {
		  }
		  bew Listener() {
		  }
		  public void handleEvent(Event e) {
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		"""
		public class Bug {
		  static boolean bold;
		  <clinit>() {
		  }
		  public Bug() {
		  }
		  public static void main(String[] arguments) {
		  }
		}
		""";

	String testName = "<10616 - local type outside method>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void _test101() {
	String s =
		"""
		public class X {\t
		    Object foo(Stack<X> s) {\t
		    }\t
		   List<T> bar(int pos, T x1, T x2, List<T> l) {\t
		    }\t
		}\t
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  Object foo() {
		  }
		  bar(int pos, T x1, T x2) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  Object foo() {
		  }
		  bar(int pos, T x1, T x2) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<12387 out of memory with generics>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test102() {
	String s =
		"""
		void ___eval() {\t
		new Runnable(){\t
		void ___run() throws Throwable {\t
		return blah;\t
		}\t
		private String blarg;\t
		public void run (){\t
				class Local {\s
					void baz() {\t
					}\t
				}\t\s
		}\t
		}\t
		;}\t
		public class Hello{\t
		private static int x;\t
		private String blah;\t
		public static void main (String[] args){\t
		}\t
		public void hello (){\t
		}\t
		public boolean blah (){\t
		return false;}\t
		public void foo (){\t
		}\t
		}\t
		""";

	String expectedDietUnitToString = """
final class handlingtoplevelanonymoustest102 {
  public class Hello {
    private static int x;
    private String blah;
    <clinit>() {
    }
    public Hello() {
    }
    public static void main(String[] args) {
    }
    public void hello() {
    }
    public boolean blah() {
    }
    public void foo() {
    }
  }
  handlingtoplevelanonymoustest102() {
  }
  void ___eval() {
  }
}
""";

	String expectedDietPlusBodyUnitToString = """
final class handlingtoplevelanonymoustest102 {
  public class Hello {
    private static int x;
    private String blah;
    <clinit>() {
    }
    public Hello() {
      super();
    }
    public static void main(String[] args) {
    }
    public void hello() {
    }
    public boolean blah() {
      return false;
    }
    public void foo() {
    }
  }
  handlingtoplevelanonymoustest102() {
    super();
  }
  void ___eval() {
    new Runnable() {
      private String blarg;
      void ___run() throws Throwable {
        return blah;
      }
      public void run() {
        class Local {
          Local() {
            super();
          }
          void baz() {
          }
        }
      }
    };
  }
}
""";

	String expectedFullUnitToString = """
final class handlingtoplevelanonymoustest102 {
  public class Hello {
    private static int x;
    private String blah;
    <clinit>() {
    }
    public Hello() {
      super();
    }
    public static void main(String[] args) {
    }
    public void hello() {
    }
    public boolean blah() {
      return false;
    }
    public void foo() {
    }
  }
  handlingtoplevelanonymoustest102() {
  }
  void ___eval() {
    new Runnable() {
      private String blarg;
      void ___run() throws Throwable {
        return blah;
      }
      public void run() {
        class Local {
          Local() {
            super();
          }
          void baz() {
          }
        }
      }
    };
  }
}
""";

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "handlingtoplevelanonymoustest102";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test103() {
	String s =
		"public class X{	\n"+
		"   void foo(int x, int y, void z";

	String expectedDietUnitToString =
		"""
		public class X {
		  void z;
		  public X() {
		  }
		  void foo(int x, int y) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  void z;
		  public X() {
		    super();
		  }
		  void foo(int x, int y) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<14038 - third argument type is void>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test104() {
	String s =
		"""
		public class P#AField {
			public void setP#A(String P#A) {
				this.P#A = P#A;
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class P {
		  {
		  }
		  public void setP;
		  public P() {
		  }
		  A(String P) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class P {
		  {
		  }
		  public void setP;
		  public P() {
		    super();
		  }
		  A(String P) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString = expectedDietUnitToString;

	String testName = "<16126>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test105() {
	String s =
		"""
		public class X {
			static int foo(int[] a, int[] b) {
				return 0;
			}
			static int B =
				foo(
					new int[]{0, 0},
					new int[]{0, 0}
				);
			#
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  static int B = foo(new int[]{0, 0}, new int[]{0, 0});
		  public X() {
		  }
		  <clinit>() {
		  }
		  static int foo(int[] a, int[] b) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  static int B = foo(new int[]{0, 0}, new int[]{0, 0});
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		  static int foo(int[] a, int[] b) {
		    return 0;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  static int B;
		  public X() {
		  }
		  <clinit>() {
		  }
		  static int foo(int[] a, int[] b) {
		  }
		}
		""";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test106() {
	String s =
		"""
		public class X {
		  clon
		  foo();
		}
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  clon foo();
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  clon foo();
		}
		""";

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  clon foo();
		}
		""";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test107() {
	String s =
		"""
		public class X {
			int[] a = new int[]{0, 0}, b = new int[]{0, 0};
			#
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  int[] a = new int[]{0, 0};
		  int[] b = new int[]{0, 0};
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int[] a = new int[]{0, 0};
		  int[] b = new int[]{0, 0};
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  int[] a;
		  int[] b;
		  public X() {
		  }
		}
		""";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test108() {
	String s =
		"""
		public class X {
			int a = new int[]{0, 0}, b = new int[]{0, 0};
			#
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  int a = new int[]{0, 0};
		  int b = new int[]{0, 0};
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  int a = new int[]{0, 0};
		  int b = new int[]{0, 0};
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  int a;
		  int b;
		  public X() {
		  }
		}
		""";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test109() {
	String s =
		"""
		public class X {
			Object o = new Object() {
				void foo() {
					try {
					} catch(Exception e) {
						e.
					}
				}
			};
		}""";

	String expectedDietUnitToString =
		"""
		public class X {
		  Object o;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  Object o;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  Object o;
		  public X() {
		  }
		}
		""";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test110() {
	String s =
		"""
		public class X {
			void bar(){
				#
				class Inner {
					void foo() {
						try {
						} catch(Exception e) {
							e.
						}
					}
				}
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  void bar() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  void bar() {
		    class Inner {
		      Inner() {
		        super();
		      }
		      void foo() {
		        try
		          {
		          }
		        catch (Exception e)
		          {
		          }
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void bar() {
		  }
		}
		""";

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test111() {
	String s =
		"""
		public class X {
			void bar(){
			}
			}
			void foo() {
			}
		}""";

	String expectedDietUnitToString =
		"""
		public class X {
		  {
		  }
		  public X() {
		  }
		  void bar() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  {
		  }
		  public X() {
		    super();
		  }
		  void bar() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100797
public void test112() {
	String s =
		"""
		public class X {
		  public void foo()
		    try {		\t
		    }  catch (Exception e) {
		     bar("blabla");
		      throw new Exception(prefix  "bloblo");
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

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		    try
		      {
		      }
		    catch (Exception e)
		      {
		        bar("blabla");
		        throw new Exception(prefix);
		      }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111618
public void test113() {
	String s =
		"""
		public class X {
		  public void foo(Object[] tab)
		    for (Object o : tab) {
				o.toString();
			 }
		  }
		}
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo(Object[] tab) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo(Object[] tab) {
		    for (Object o : tab)\s
		      {
		        o.toString();
		      }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129142
public void test114() {
	String s =
		"""
		public class X {
		  public void foo() {
		    int int;
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

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		    int $missing$;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test115() {
	String s =
		"""
		public interface Test {
		  public void myMethod()
		}
		""";

	String expectedDietUnitToString =
		"""
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test116() {
	String s =
		"""
		public interface Test {
		  public void myMethod()
		    System.out.println();
		}
		""";

	String expectedDietUnitToString =
		"""
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public interface Test {
		  public void myMethod() {
		    System.out.println();
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public interface Test {
		  public void myMethod() {
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154811
public void test117() {
	String s =
		"""
		public class X {
			void foo1() {
				class Y  {
				}
				void foo2() {
				}
				class Z<T> {\s
				}
			}
		}\s
		""";

	String expectedDietUnitToString = null;
	String expectedDietPlusBodyUnitToString = null;
	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString = null;
	String expectedFullUnitToString = null;
	String expectedCompletionDietUnitToString = null;


	if(this.complianceLevel <= ClassFileConstants.JDK1_4) {

		expectedDietUnitToString =
			"""
				public class X {
				  public X() {
				  }
				  void foo1() {
				  }
				}
				""";

		expectedDietPlusBodyUnitToString =
			"""
				public class X {
				  public X() {
				    super();
				  }
				  void foo1() {
				  }
				}
				""";

		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"""
				public class X {
				  public X() {
				    super();
				  }
				  void foo1() {
				    class Y {
				      Y() {
				        super();
				      }
				    }
				    class Z<T> {
				      Z() {
				        super();
				      }
				    }
				  }
				}
				""";

		expectedFullUnitToString =
			"""
				public class X {
				  class Z<T> {
				    Z() {
				    }
				  }
				  public X() {
				  }
				  void foo1() {
				  }
				  void foo2() {
				  }
				}
				""";

		expectedCompletionDietUnitToString =
			expectedDietUnitToString;
	} else if(this.complianceLevel >= ClassFileConstants.JDK1_5) {

		expectedDietUnitToString =
			"""
				public class X {
				  public X() {
				  }
				  void foo1() {
				  }
				}
				""";

		expectedDietPlusBodyUnitToString =
			"""
				public class X {
				  public X() {
				    super();
				  }
				  void foo1() {
				  }
				}
				""";

		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  void foo1() {\n" +
			"    class Y {\n" +
			"      Y() {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n" +
			(this.complianceLevel < ClassFileConstants.JDK14
			?
			"    new foo2() {\n" +
			"    };\n" +
			"    class Z<T> {\n" +
			"      Z() {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n"
			:
			"    void foo2;\n"
			) +
			"  }\n" +
			"}\n";

		expectedFullUnitToString =
			"""
				public class X {
				  class Z<T> {
				    Z() {
				    }
				  }
				  public X() {
				  }
				  void foo1() {
				  }
				  void foo2() {
				  }
				}
				""";

		expectedCompletionDietUnitToString =
			expectedDietUnitToString;
	}

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154811
public void test117_2() {
	String s =
		"""
		public class X {
			void foo1() {
				class Y  {
				}
				void foo2() {
				}
				class Z {\s
				}
			}
		}\s
		""";

	String expectedDietUnitToString = null;
	String expectedDietPlusBodyUnitToString = null;
	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString = null;
	String expectedFullUnitToString = null;
	String expectedCompletionDietUnitToString = null;

	expectedDietUnitToString =
		"""
			public class X {
			  public X() {
			  }
			  void foo1() {
			  }
			}
			""";

	expectedDietPlusBodyUnitToString =
		"""
			public class X {
			  public X() {
			    super();
			  }
			  void foo1() {
			  }
			}
			""";

	expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void foo1() {\n" +
		"    class Y {\n" +
		"      Y() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"    new foo2() {\n" +
		"    };\n" +
		"    class Z {\n" +
		"      Z() {\n" +
		"        super();\n" +
		"      }\n" +
		"    }\n"
		:
		"    void foo2;\n"
		) +
		"  }\n" +
		"}\n";

	expectedFullUnitToString =
		"""
			public class X {
			  class Z {
			    Z() {
			    }
			  }
			  public X() {
			  }
			  void foo1() {
			  }
			  void foo2() {
			  }
			}
			""";

	expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162056
public void test118() {
	String s =
		"""
		interface Irrelevant {}
		interface I {
			Object foo(Number n);
		}
		interface J extends I {
			String foo(Number n);
		}
		interface K {
			Object foo(Number n);
		}
		public class  {
			void foo() {
		
			}
		}\s
		""";

	String expectedDietUnitToString =
		"""
		interface Irrelevant {
		}
		interface I {
		  Object foo(Number n);
		}
		interface J extends I {
		  String foo(Number n);
		}
		interface K {
		  Object foo(Number n);
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		interface Irrelevant {
		}
		interface I {
		  Object foo(Number n);
		}
		interface J extends I {
		  String foo(Number n);
		}
		interface K {
		  Object foo(Number n);
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162056
public void test119() {
	String s =
		"""
		interface Irrelevant {}
		interface I {
			Object foo(Number n);
		}
		interface J extends I {
			String foo(Number n);
		}
		abstract class K {
			abstract Object foo(Number n);
		}
		public class  {
			void foo() {
		
			}
		}\s
		""";

	String expectedDietUnitToString =
		"""
		interface Irrelevant {
		}
		interface I {
		  Object foo(Number n);
		}
		interface J extends I {
		  String foo(Number n);
		}
		abstract class K {
		  {
		  }
		  K() {
		  }
		  abstract Object foo(Number n);
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		interface Irrelevant {
		}
		interface I {
		  Object foo(Number n);
		}
		interface J extends I {
		  String foo(Number n);
		}
		abstract class K {
		  {
		  }
		  K() {
		    super();
		  }
		  abstract Object foo(Number n);
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test120() {
	String s =
		"""
		public class X {
		  void foo() {
		    #
		    try {
		      System.out.println();\s
		    } catch (Exception e) {
		    }
		    class Z {}
		 }
		}
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
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
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    try
		      {
		        System.out.println();
		      }
		    catch (Exception e)
		      {
		      }
		    class Z {
		      Z() {
		        super();
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test121() {
	String s =
		"""
		public class X {
		  void foo() {
		    #
		    try {
		      System.out.println();\s
		    } catch (Exception e) {
		      class Z {}
		    }
		 }
		}
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
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
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    try
		      {
		        System.out.println();
		      }
		    catch (Exception e)
		      {
		        class Z {
		          Z() {
		            super();
		          }
		        }
		      }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test122() {
	String s =
		"""
		public class Test
		{
		  public void func1()
		  {
		    try
		    {
		    catch ( Exception exception)
		    {
		      exception.printStackTrace();
		    }
		  }
		
		  class Clazz
		  {
		  }
		}
		
		""";

	String expectedDietUnitToString =
		"""
		public class Test {
		  public Test() {
		  }
		  public void func1() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class Test {
		  public Test() {
		    super();
		  }
		  public void func1() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class Test {
		  public Test() {
		    super();
		  }
		  public void func1() {
		    try
		      {
		      }
		    catch (Exception exception)
		      {
		        exception.printStackTrace();
		      }
		    class Clazz {
		      Clazz() {
		        super();
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=140980
public void test123() {
	String s =
		"""
		public class SwitchBug {
		       void aMethod() {
		               int i=0;
		               try {
		                        switch( i ) {
		                } catch( Exception ex ) {
		                }
		        }
		        class Nested {
		        }
		}
		""";

	String expectedDietUnitToString =
		"""
		public class SwitchBug {
		  public SwitchBug() {
		  }
		  void aMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class SwitchBug {
		  public SwitchBug() {
		    super();
		  }
		  void aMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		public class SwitchBug {
		  public SwitchBug() {
		    super();
		  }
		  void aMethod() {
		    int i = 0;
		    try
		      {
		        switch (i) {
		        }
		      }
		    catch (Exception ex)
		      {
		      }
		    class Nested {
		      Nested() {
		        super();
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test foreach toString";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=157570
public void _test124() {
	String s =
		"""
		public class Test {
			void aMethod() {
				public static void m1()
				{
					int a;
					int b;
				}
				public static void m2()
				{
					int c;
					int d;
				}
			}
		}
		""";

	String expectedDietUnitToString =
		"""
		public class Test {
		  public Test() {
		  }
		  void aMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class Test {
		  public Test() {
		    super();
		  }
		  void aMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString = null;
	if(this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"""
				public class Test {
				  public Test() {
				    super();
				  }
				  void aMethod() {
				    m1();
				    {
				      int a;
				      int b;
				    }
				    m2();
				    {
				      int c;
				      int d;
				    }
				  }
				}
				""";
	} else {
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"""
				public class Test {
				  public Test() {
				    super();
				  }
				  void aMethod() {
				  }
				}
				""";
	}

	String expectedFullUnitToString =
		"""
		public class Test {
		  public Test() {
		  }
		  void aMethod() {
		  }
		  public static void m1() {
		  }
		  public static void m2() {
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=271680
public void test125() {
	String s =
		"public class Test {\n" +
		"}\n";

	StringBuilder buf = new StringBuilder();
	for (int i = 0; i < 1000; i++) {
		buf.append("class AClass #\n");
	}
	s+= buf.toString();

	// expectedDietUnitToString
	String expectedDietUnitToString =
		"""
		public class Test {
		  public Test() {
		  }
		}
		""";
	buf = new StringBuilder();
	int max = 256;
	for (int i = 0; i < max; i++) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("class AClass {\n");
	}
	for (int i = max - 1; i >= 0; i--) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("  AClass() {\n");
		buf.append(indent).append("  }\n");
		buf.append(indent).append("}\n");
	}

	expectedDietUnitToString += buf.toString();

	// expectedDietPlusBodyUnitToString
	String expectedDietPlusBodyUnitToString =
		"""
		public class Test {
		  public Test() {
		    super();
		  }
		}
		""";
	buf = new StringBuilder();
	for (int i = 0; i < max; i++) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("class AClass {\n");
	}
	for (int i = max - 1; i >= 0; i--) {
		String indent = "";
		for (int j = 0; j < i; j++) {
			indent+= "  ";
		}
		buf.append(indent).append("  AClass() {\n");
		buf.append(indent).append("    super();\n");
		buf.append(indent).append("  }\n");
		buf.append(indent).append("}\n");
	}
	expectedDietPlusBodyUnitToString += buf.toString();

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132679
public void test126() {
	String s =
		"""
		package p;
		public class ContextTest {
		  private Context context = new Context();
		  public void test() {
		      context.new Callback() {
		      public void doit(int value) {
		       #
		      }
		    };
		  }
		}
		""";

	String expectedDietUnitToString =
		"""
		package p;
		public class ContextTest {
		  private Context context = new Context();
		  public ContextTest() {
		  }
		  public void test() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package p;
		public class ContextTest {
		  private Context context = new Context();
		  public ContextTest() {
		    super();
		  }
		  public void test() {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package p;
		public class ContextTest {
		  private Context context = new Context();
		  public ContextTest() {
		    super();
		  }
		  public void test() {
		    context.new Callback() {
		      public void doit(int value) {
		      }
		    };
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package p;
		public class ContextTest {
		  private Context context;
		  public ContextTest() {
		  }
		  public void test() {
		  }
		}
		""";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201762
public void test127() {
	String s =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		
		public class Try {
		
		    void main(Shell shell) {
		
		        final Label label= new Label(shell, SWT.WRAP);
		        label.addPaintListener(new PaintListener() {
		            public void paintControl(PaintEvent e) {
		                e.gc.setLineCap(SWT.CAP_); // content assist after CAP_
		            }
		        });
		
		        shell.addControlListener(new ControlAdapter() { });
		
		        while (!shell.isDisposed()) { }
		    }
		}
		
		""";

	String expectedDietUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		  }
		  void main(Shell shell) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		    super();
		  }
		  void main(Shell shell) {
		    final Label label = new Label(shell, SWT.WRAP);
		    label.addPaintListener(new PaintListener() {
		  public void paintControl(PaintEvent e) {
		    e.gc.setLineCap(SWT.CAP_);
		  }
		});
		    shell.addControlListener(new ControlAdapter() {
		});
		    while ((! shell.isDisposed()))      {
		      }
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		    super();
		  }
		  void main(Shell shell) {
		    final Label label = new Label(shell, SWT.WRAP);
		    label.addPaintListener(new PaintListener() {
		  public void paintControl(PaintEvent e) {
		    e.gc.setLineCap(SWT.CAP_);
		  }
		});
		    shell.addControlListener(new ControlAdapter() {
		});
		    while ((! shell.isDisposed()))      {
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		    super();
		  }
		  void main(Shell shell) {
		    final Label label = new Label(shell, SWT.WRAP);
		    label.addPaintListener(new PaintListener() {
		  public void paintControl(PaintEvent e) {
		    e.gc.setLineCap(SWT.CAP_);
		  }
		});
		    shell.addControlListener(new ControlAdapter() {
		});
		    while ((! shell.isDisposed()))      {
		      }
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		  }
		  void main(Shell shell) {
		  }
		}
		""";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201762
public void test128() {
	String s =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		
		public class Try {
		
		    void main(Shell shell) {
		
		        final Label label= new Label(shell, SWT.WRAP);
		        label.addPaintListener(new PaintListener() {
		            public void paintControl(PaintEvent e) {
		                e.gc.setLineCap(SWT.CAP_#); // content assist after CAP_
		            }
		        });
		
		        shell.addControlListener(new ControlAdapter() { });
		
		        while (!shell.isDisposed()) { }
		    }
		}
		
		""";

	String expectedDietUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		  }
		  void main(Shell shell) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		    super();
		  }
		  void main(Shell shell) {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		    super();
		  }
		  void main(Shell shell) {
		    final Label label = new Label(shell, SWT.WRAP);
		    label.addPaintListener(new PaintListener() {
		  public void paintControl(PaintEvent e) {
		    e.gc.setLineCap(SWT.CAP_);
		  }
		});
		    shell.addControlListener(new ControlAdapter() {
		});
		    while ((! shell.isDisposed()))      {
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		  }
		  void main(Shell shell) {
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		"""
		import org.eclipse.swt.*;
		import org.eclipse.swt.events.*;
		import org.eclipse.swt.widgets.*;
		public class Try {
		  public Try() {
		  }
		  void main(Shell shell) {
		  }
		}
		""";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778 - [1.8][dom ast] method body recovery broken (empty body)
public void test405778() {
		String s =
			"""
			import java.util.Collection;
			public class E {
			    public void __test1() {
			        Object o = new Object();
			        if (o.hashCode() != 0) {
			           o.
			        \s
			        }
			     }
			}\
			
			""";

		String expectedDietUnitToString =
			"""
			import java.util.Collection;
			public class E {
			  public E() {
			  }
			  public void __test1() {
			  }
			}
			""";

		String expectedDietPlusBodyUnitToString =
			"""
			import java.util.Collection;
			public class E {
			  public E() {
			    super();
			  }
			  public void __test1() {
			  }
			}
			""";

		String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
			"""
			import java.util.Collection;
			public class E {
			  public E() {
			    super();
			  }
			  public void __test1() {
			    Object o = new Object();
			    if ((o.hashCode() != 0))
			        {
			          o = $missing$;
			        }
			  }
			}
			""";

		String expectedFullUnitToString =
			"""
			import java.util.Collection;
			public class E {
			  public E() {
			  }
			  public void __test1() {
			  }
			}
			""";

		String expectedCompletionDietUnitToString =
			"""
			import java.util.Collection;
			public class E {
			  public E() {
			  }
			  public void __test1() {
			  }
			}
			""";

		String testName = "test";
		checkParse(
			s.toCharArray(),
			expectedDietUnitToString,
			expectedDietPlusBodyUnitToString,
			expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
			expectedFullUnitToString,
			expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405778 - [1.8][dom ast] method body recovery broken (empty body)
public void test405778a() {
	String s =
		"""
		import java.util.Collection;
		public class E {
		    void m(String[] names) {
		/*[*/
		for (String string : names) {
		System.out.println(string.);
		}
		/*]*/
		}
		}
		
		""";

	String expectedDietUnitToString =
		"""
		import java.util.Collection;
		public class E {
		  public E() {
		  }
		  void m(String[] names) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import java.util.Collection;
		public class E {
		  public E() {
		    super();
		  }
		  void m(String[] names) {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		import java.util.Collection;
		public class E {
		  public E() {
		    super();
		  }
		  void m(String[] names) {
		    for (String string : names)\s
		      {
		        System.out.println(string.class);
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		import java.util.Collection;
		public class E {
		  public E() {
		  }
		  void m(String[] names) {
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		"""
		import java.util.Collection;
		public class E {
		  public E() {
		  }
		  void m(String[] names) {
		  }
		}
		""";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=456861 - [recovery] NPE in RecoveryScanner since Mars M4
public void test456861() {
	String s =
		"""
		import java.awt.Point;
		public class Test {
			public void foo(Point p, int[] a) {
				String s1 = "";
				s.;
			}
		 }""";

	String expectedDietUnitToString =
		"""
		import java.awt.Point;
		public class Test {
		  public Test() {
		  }
		  public void foo(Point p, int[] a) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		import java.awt.Point;
		public class Test {
		  public Test() {
		    super();
		  }
		  public void foo(Point p, int[] a) {
		  }
		}
		""";

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		import java.awt.Point;
		public class Test {
		  public Test() {
		    super();
		  }
		  public void foo(Point p, int[] a) {
		    String s1 = "";
		    s = $missing$;
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		import java.awt.Point;
		public class Test {
		  public Test() {
		  }
		  public void foo(Point p, int[] a) {
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		"""
		import java.awt.Point;
		public class Test {
		  public Test() {
		  }
		  public void foo(Point p, int[] a) {
		  }
		}
		""";

	String testName = "test";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
}
