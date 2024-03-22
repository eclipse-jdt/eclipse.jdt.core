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
public class EnumDietRecoveryTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

public EnumDietRecoveryTest(String testName){
	super(testName);
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
		#
		public enum X {							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#
		public enum X {							\t
		  A,										\t
		  B;										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  A(),
		  B(),
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  A(),
		  B(),
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#
		public enum X {							\t
		  A(10),									\t
		  B(){};									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  A(10),
		  B() {
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  A(10),
		  B() {
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  }  									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0005() {

	String s =
		"""
		package a;										\t
		#
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  };  									\t
		  public X(){}							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  <clinit>() {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  <clinit>() {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  }  									\t
		  public X(){} 							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  public X() {
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  public X() {
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  }  									\t
		  X(){} 							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  X() {
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  X() {
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  }  									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  <clinit>() {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  <clinit>() {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#              							\t
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  }  									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
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
		#              							\t
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  ;  									\t
		  void bar(){}  							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		    void bar() {
		    }
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		    void bar() {
		    }
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0011() {

	String s =
		"""
		package a;										\t
		#              							\t
		public enum X {							\t
		  B(){									\t
		    void foo(){							\t
		    }									\t
		  ;  									\t
		  X(){}      							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		    X() {
		    }
		  },
		  public X() {
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X {
		  B() {
		    void foo() {
		    }
		    X() {
		    }
		  },
		  public X() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0012() {

	String s =
		"""
		package a;										\t
		#              							\t
		public enum X {							\t
		  B()									\t
		    void foo(){							\t
		    }									\t
		  };  									\t
		  void bar(){}  							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B(),
		  {
		  }
		  public X() {
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
		package a;
		public enum X {
		  B(),
		  {
		  }
		  public X() {
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

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0013() {

	String s =
		"""
		package a;										\t
		#              							\t
		public enum X {							\t
		  B( {									\t
		    void foo(){							\t
		    }									\t
		  };  									\t
		  void bar(){}  							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X {
		  B,
		  {
		  }
		  public X() {
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
		package a;
		public enum X {
		  B,
		  {
		  }
		  public X() {
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

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0014() {

	String s =
		"""
		package a;										\t
		#              							\t
		public class X {							\t
		  class Y { 								\t
		  }   									\t
		  enum Z {								\t
		    B() {								\t
		      void foo(){						\t
		      }									\t
		    };  									\t
		    Z(){}       							\t
		  }            							\t
		  class W {     							\t
		  }             							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  class Y {
		    Y() {
		    }
		  }
		  enum Z {
		    B() {
		      void foo() {
		      }
		    },
		    <clinit>() {
		    }
		    Z() {
		    }
		  }
		  class W {
		    W() {
		    }
		  }
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  class Y {
		    Y() {
		      super();
		    }
		  }
		  enum Z {
		    B() {
		      void foo() {
		      }
		    },
		    <clinit>() {
		    }
		    Z() {
		      super();
		    }
		  }
		  class W {
		    W() {
		      super();
		    }
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76874
 */
public void test0015() {

	String s =
		"""
		public enum Enum1 {					\t
		  BLEU(){   								\t
		    void foo() {                            \s
		       System.out.println();     		\t
		    }    								\t
		  },             						\t
		  BLANC,  								\t
		  ROUGE;									\t
		                							\t
		  main         							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public enum Enum1 {
		  BLEU() {
		    void foo() {
		    }
		  },
		  BLANC(),
		  ROUGE(),
		  public Enum1() {
		  }
		  <clinit>() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		public enum Enum1 {
		  BLEU() {
		    void foo() {
		    }
		  },
		  BLANC(),
		  ROUGE(),
		  public Enum1() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=107580
 */
public void test0016() {
	String s =
		"""
		public enum Enum {							\t
		  BEGIN("blabla"),					\t
		  END("blabla").						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		public enum Enum {
		  BEGIN("blabla"),
		  END("blabla"),
		  public Enum() {
		  }
		  <clinit>() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		public enum Enum {
		  BEGIN("blabla"),
		  END("blabla"),
		  public Enum() {
		    super();
		  }
		  <clinit>() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0017() {

	String s =
		"""
		package a;										\t
		public enum X <T> {						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public enum X<T> {
		  <clinit>() {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public enum X<T> {
		  <clinit>() {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		"""
		package a;
		public enum X<T> {
		  <clinit>() {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<enum type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
}
