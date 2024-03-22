/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CombinedBinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * A tests series especially meant to validate the internals of our AST
 * implementation.
 */
@SuppressWarnings({ "rawtypes" })
public class ASTImplTests extends AbstractRegressionTest {
public ASTImplTests(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test2050" };
//    	TESTS_NUMBERS = new int[] { 3 };
//    	TESTS_NUMBERS = new int[] { 2999 };
//    	TESTS_RANGE = new int[] { 2050, -1 };
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
    return ASTImplTests.class;
}

// Helper methods
static Parser defaultParser = new Parser(
			new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			new CompilerOptions(),
			new DefaultProblemFactory()), false);
public void runConformTest(String fileName, String fileContents,
		Parser parser, ASTCollector visitor, String expected) {
	CompilationUnit source =
		new CompilationUnit(fileContents.toCharArray(),	fileName, null);
	CompilationResult compilationResult =
		new CompilationResult(source, 1, 1, 10);
	CompilationUnitDeclaration unit = parser.parse(source, compilationResult);
	assertEquals(0, compilationResult.problemCount);
	unit.traverse(visitor, unit.scope);
	String result = visitor.result();
	if (! expected.equals(result)) {
		System.out.println(getClass().getName() + '#' + getName());
		System.out.println("Expected:");
		System.out.println(expected);
		System.out.println("But was:");
		System.out.println(result);
		System.out.println("Cut and paste:");
		System.out.println(Util.displayString(result, INDENT, SHIFT));
	}
	assertEquals(expected, result);
}

// AST implementation - visiting binary expressions
public void test0001_regular_binary_expression() {
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    String s3 = "s3";
			    String s4 = "s4";
			    System.out.println(s1 + "l1" + s2 + "l2" +
			      s3 + "l3" + s4);
			  }
			}
			""",
		defaultParser,
		new ASTBinaryExpressionCollector(),
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v SL "s2"]
			[ev SL "s2"]
			[v SL "s3"]
			[ev SL "s3"]
			[v SL "s4"]
			[ev SL "s4"]
			[v BE ((((((s1 + "l1") + s...) + s4)]
			[v BE (((((s1 + "l1") + s2...+ "l3")]
			[v BE ((((s1 + "l1") + s2)...) + s3)]
			[v BE (((s1 + "l1") + s2) + "l2")]
			[v BE ((s1 + "l1") + s2)]
			[v BE (s1 + "l1")]
			[v SNR s1]
			[ev SNR s1]
			[v SL "l1"]
			[ev SL "l1"]
			[ev BE (s1 + "l1")]
			[v SNR s2]
			[ev SNR s2]
			[ev BE ((s1 + "l1") + s2)]
			[v SL "l2"]
			[ev SL "l2"]
			[ev BE (((s1 + "l1") + s2) + "l2")]
			[v SNR s3]
			[ev SNR s3]
			[ev BE ((((s1 + "l1") + s2)...) + s3)]
			[v SL "l3"]
			[ev SL "l3"]
			[ev BE (((((s1 + "l1") + s2...+ "l3")]
			[v SNR s4]
			[ev SNR s4]
			[ev BE ((((((s1 + "l1") + s...) + s4)]
			""");
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions
public void test0002_combined_binary_expression() {
	CombinedBinaryExpression.defaultArityMaxStartingValue = 3;
	// one CBE each fourth BE
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    String s3 = "s3";
			    String s4 = "s4";
			    System.out.println(s1 + "l1" + s2 + "l2" +
			      s3 + "l3" + s4);
			  }
			}
			""",
		defaultParser,
		new ASTBinaryExpressionCollector() {
			@Override
			public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
				if (binaryExpression instanceof CombinedBinaryExpression &&
						((CombinedBinaryExpression) binaryExpression).
							referencesTable != null) {
					this.collector.append("[ev CBE " +
						cut(binaryExpression.toString()) + "]\n");
				} else {
					super.endVisit(binaryExpression, scope);
				}
			}
		},
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v SL "s2"]
			[ev SL "s2"]
			[v SL "s3"]
			[ev SL "s3"]
			[v SL "s4"]
			[ev SL "s4"]
			[v BE ((((((s1 + "l1") + s...) + s4)]
			[v BE (((((s1 + "l1") + s2...+ "l3")]
			[v BE ((((s1 + "l1") + s2)...) + s3)]
			[v BE (((s1 + "l1") + s2) + "l2")]
			[v BE ((s1 + "l1") + s2)]
			[v BE (s1 + "l1")]
			[v SNR s1]
			[ev SNR s1]
			[v SL "l1"]
			[ev SL "l1"]
			[ev BE (s1 + "l1")]
			[v SNR s2]
			[ev SNR s2]
			[ev BE ((s1 + "l1") + s2)]
			[v SL "l2"]
			[ev SL "l2"]
			[ev BE (((s1 + "l1") + s2) + "l2")]
			[v SNR s3]
			[ev SNR s3]
			[ev CBE ((((s1 + "l1") + s2)...) + s3)]
			[v SL "l3"]
			[ev SL "l3"]
			[ev BE (((((s1 + "l1") + s2...+ "l3")]
			[v SNR s4]
			[ev SNR s4]
			[ev BE ((((((s1 + "l1") + s...) + s4)]
			""");
	CombinedBinaryExpression.defaultArityMaxStartingValue =
		CombinedBinaryExpression.ARITY_MAX_MIN;
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions
public void test0003_combined_binary_expression() {
	Parser parser = new Parser(
			new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			new CompilerOptions(),
			new DefaultProblemFactory()), true); // optimize string literals
	CombinedBinaryExpression.defaultArityMaxStartingValue = 2;
		// one CBE each third BE - except the top one, which is degenerate (no
		// references table)
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    String s3 = "s3";
			    String s4 = "s4";
			    System.out.println(s1 + "l1" + s2 + "l2" +
			      s3 + "l3" + s4);
			  }
			}
			""",
		parser,
		new ASTBinaryExpressionCollector() {
			@Override
			public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
				if (binaryExpression instanceof CombinedBinaryExpression &&
						((CombinedBinaryExpression) binaryExpression).
							referencesTable != null) {
					this.collector.append("[ev CBE " +
						cut(binaryExpression.toString()) + "]\n");
				} else {
					super.endVisit(binaryExpression, scope);
				}
			}
		},
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v SL "s2"]
			[ev SL "s2"]
			[v SL "s3"]
			[ev SL "s3"]
			[v SL "s4"]
			[ev SL "s4"]
			[v BE ((((((s1 + "l1") + s...) + s4)]
			[v BE (((((s1 + "l1") + s2...+ "l3")]
			[v BE ((((s1 + "l1") + s2)...) + s3)]
			[v BE (((s1 + "l1") + s2) + "l2")]
			[v BE ((s1 + "l1") + s2)]
			[v BE (s1 + "l1")]
			[v SNR s1]
			[ev SNR s1]
			[v SL "l1"]
			[ev SL "l1"]
			[ev BE (s1 + "l1")]
			[v SNR s2]
			[ev SNR s2]
			[ev BE ((s1 + "l1") + s2)]
			[v SL "l2"]
			[ev SL "l2"]
			[ev CBE (((s1 + "l1") + s2) + "l2")]
			[v SNR s3]
			[ev SNR s3]
			[ev BE ((((s1 + "l1") + s2)...) + s3)]
			[v SL "l3"]
			[ev SL "l3"]
			[ev BE (((((s1 + "l1") + s2...+ "l3")]
			[v SNR s4]
			[ev SNR s4]
			[ev BE ((((((s1 + "l1") + s...) + s4)]
			""");
	CombinedBinaryExpression.defaultArityMaxStartingValue =
		CombinedBinaryExpression.ARITY_MAX_MIN;
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - effect of a literal at the start with
// string literal optimization
public void test0004_combined_binary_expression() {
	Parser parser = new Parser(
			new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			new CompilerOptions(),
			new DefaultProblemFactory()), true); // optimize string literals
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    System.out.println("l" + "1" + s1);
			  }
			}
			""",
		parser,
		new ASTBinaryExpressionCollector(),
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v BE (ExtendedStringLiter...} + s1)]
			[v ESL ExtendedStringLiteral{l1}]
			[ev ESL ExtendedStringLiteral{l1}]
			[v SNR s1]
			[ev SNR s1]
			[ev BE (ExtendedStringLiter...} + s1)]
			""");
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - effect of a literal at the start without
// string literals optimization
public void test0005_combined_binary_expression() {
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    System.out.println("l" + "1" + s1);
			  }
			}
			""",
		defaultParser,
		new ASTBinaryExpressionCollector(),
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v BE (StringLiteralConcat...} + s1)]
			[v SLC StringLiteralConcate...
			"1"+
			}]
			[v SL "l"]
			[ev SL "l"]
			[v SL "1"]
			[ev SL "1"]
			[ev SLC StringLiteralConcate...
			"1"+
			}]
			[v SNR s1]
			[ev SNR s1]
			[ev BE (StringLiteralConcat...} + s1)]
			""");
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - cutting the traversal half-way down
public void test0006_combined_binary_expression() {
	CombinedBinaryExpression.defaultArityMaxStartingValue = 1;
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    String s3 = "s3";
			    String s4 = "s4";
			    System.out.println(s1 + "l1" + s2 + "l2" +
			      s3 + s1 + s4);
			  }
			}
			""",
		defaultParser,
		new ASTBinaryExpressionCollector() {
			@Override
			public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
				super.visit(binaryExpression, scope);
				if (binaryExpression.right instanceof StringLiteral) {
					return false;
				}
				return true;
			}
		},
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v SL "s2"]
			[ev SL "s2"]
			[v SL "s3"]
			[ev SL "s3"]
			[v SL "s4"]
			[ev SL "s4"]
			[v BE ((((((s1 + "l1") + s...) + s4)]
			[v BE (((((s1 + "l1") + s2...) + s1)]
			[v BE ((((s1 + "l1") + s2)...) + s3)]
			[v BE (((s1 + "l1") + s2) + "l2")]
			[ev BE (((s1 + "l1") + s2) + "l2")]
			[v SNR s3]
			[ev SNR s3]
			[ev BE ((((s1 + "l1") + s2)...) + s3)]
			[v SNR s1]
			[ev SNR s1]
			[ev BE (((((s1 + "l1") + s2...) + s1)]
			[v SNR s4]
			[ev SNR s4]
			[ev BE ((((((s1 + "l1") + s...) + s4)]
			""");
	CombinedBinaryExpression.defaultArityMaxStartingValue =
		CombinedBinaryExpression.ARITY_MAX_MIN;
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - cutting the traversal right away
public void test0007_combined_binary_expression() {
	CombinedBinaryExpression.defaultArityMaxStartingValue = 4;
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    String s3 = "s3";
			    String s4 = "s4";
			    System.out.println(s1 + "l1" + s2 + "l2" +
			      s3 + "l3" + s4);
			  }
			}
			""",
		defaultParser,
		new ASTBinaryExpressionCollector() {
			@Override
			public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
				super.visit(binaryExpression, scope);
				return false;
			}
		},
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v SL "s2"]
			[ev SL "s2"]
			[v SL "s3"]
			[ev SL "s3"]
			[v SL "s4"]
			[ev SL "s4"]
			[v BE ((((((s1 + "l1") + s...) + s4)]
			[ev BE ((((((s1 + "l1") + s...) + s4)]
			""");
	CombinedBinaryExpression.defaultArityMaxStartingValue =
		CombinedBinaryExpression.ARITY_MAX_MIN;
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - case of one-deep expression
public void test0008_combined_binary_expression() {
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    System.out.println(s1 + "l1" + s2 + "l2");
			    System.out.println(s1 + s2);
			  }
			}
			""",
		defaultParser,
		new ASTBinaryExpressionCollector() {
			@Override
			public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
				if (binaryExpression instanceof CombinedBinaryExpression) {
					this.collector.append("[ev CBE " +
						cut(binaryExpression.toString()) + "]\n");
				} else {
					super.endVisit(binaryExpression, scope);
				}
			}
		},
		"""
			[v SL "s1"]
			[ev SL "s1"]
			[v SL "s2"]
			[ev SL "s2"]
			[v BE (((s1 + "l1") + s2) + "l2")]
			[v BE ((s1 + "l1") + s2)]
			[v BE (s1 + "l1")]
			[v SNR s1]
			[ev SNR s1]
			[v SL "l1"]
			[ev SL "l1"]
			[ev BE (s1 + "l1")]
			[v SNR s2]
			[ev SNR s2]
			[ev BE ((s1 + "l1") + s2)]
			[v SL "l2"]
			[ev SL "l2"]
			[ev CBE (((s1 + "l1") + s2) + "l2")]
			[v BE (s1 + s2)]
			[v SNR s1]
			[ev SNR s1]
			[v SNR s2]
			[ev SNR s2]
			[ev BE (s1 + s2)]
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if the generated code is OK when leveraging CombinedBinaryExpression
public void test0009_combined_binary_expression() {
	assertEquals(20, CombinedBinaryExpression.ARITY_MAX_MIN);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				public static void main(String args[]) {
				    final int max = 30;\s
				    String s[] = new String[max];
				    for (int i = 0; i < max; i++) {
				        s[i] = "a";
				    }
				    foo(s);
				}
				static void foo (String s[]) {
				    System.out.println(
				        s[0] + s[1] + s[2] + s[3] + s[4] + s[5] + s[6] +\s
				        s[7] + s[8] + s[9] + s[10] + s[11] + s[12] + s[13] +
				        s[14] + s[15] + s[16] + s[17] + s[18] + s[19] +\s
				        s[20] + s[21] + s[22] + s[23] + s[24] + s[25] +\s
				        s[26] + s[27] + s[28] + s[29]
				        );
				}
				}"""},
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if the generated code is OK when leveraging CombinedBinaryExpression
// variant involving constant binary expressions deep in the tree
public void test0010_combined_binary_expression() {
	assertEquals(20, CombinedBinaryExpression.ARITY_MAX_MIN);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				public static void main(String args[]) {
				    final int max = 30;\s
				    String s[] = new String[max];
				    for (int i = 0; i < max; i++) {
				        s[i] = "a";
				    }
				    foo(s);
				}
				static void foo (String s[]) {
				    final String c = "a";\
				    System.out.println(
				        c + c + c + c + s[4] + s[5] + s[6] + s[7] + s[8] +\s
				        s[9] + s[10] + s[11] + s[12] + s[13] + s[14] +\s
				        s[15] + s[16] + s[17] + s[18] + s[19] + s[20] +\s
				        s[21] + s[22] + s[23] + s[24] + s[25] + s[26] +\s
				        s[27] + s[28] + s[29]
				        );
				}
				}"""
		},
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if the generated code is OK when leveraging CombinedBinaryExpression
// variant involving a constant combined binary expression
public void test0011_combined_binary_expression() {
	assertEquals(20, CombinedBinaryExpression.ARITY_MAX_MIN);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				public static void main(String args[]) {
				    final int max = 30;\s
				    String s[] = new String[max];
				    for (int i = 0; i < max; i++) {
				        s[i] = "a";
				    }
				    foo(s);
				}
				static void foo (String s[]) {
				    final String c = "a";\
				    System.out.println(
				        c + c + c + c + c + c + c + c + c + c +\s
				        c + c + c + c + c + c + c + c + c + c +\s
				        c + c + s[22] + s[23] + s[24] + s[25] + s[26] +\s
				        s[27] + s[28] + s[29]
				        );
				}
				}"""
		},
		"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
}

// AST implementation - visiting binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - checking recursive print
public void test0012_combined_binary_expression() {
	CombinedBinaryExpression.defaultArityMaxStartingValue = 2;
	runConformTest(
		"X.java",
		"""
			public class X {
			  void foo() {
			    String s1 = "s1";
			    String s2 = "s2";
			    String s3 = "s3";
			    String s4 = "s4";
			    System.out.println(s1 + "l1" + s2 + "l2" +
			      s3 + s1 + s4);
			  }
			}
			""",
		defaultParser,
		new ASTCollector() {
			public boolean visit(BinaryExpression binaryExpression,
					BlockScope scope) {
				super.visit(binaryExpression, scope);
				this.collector.append(binaryExpression);
				return true;
			}
		},
		"""
			((((((s1 + "l1") + s2) + "l2") + s3) + s1) + s4)(((((s1 + "l1")\
			 + s2) + "l2") + s3) + s1)((((s1 + "l1") + s2) + "l2") + s3)\
			(((s1 + "l1") + s2) + "l2")((s1 + "l1") + s2)(s1 + "l1")""");
	CombinedBinaryExpression.defaultArityMaxStartingValue =
		CombinedBinaryExpression.ARITY_MAX_MIN;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if the generated code is OK when leveraging CombinedBinaryExpression
// variant involving a left-deep right expression at the topmost level
public void test0013_combined_binary_expression() {
	assertEquals(20, CombinedBinaryExpression.ARITY_MAX_MIN);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				public static void main(String args[]) {
				    final int max = 30;\s
				    String s[] = new String[max];
				    for (int i = 0; i < max; i++) {
				        s[i] = "a";
				    }
				    foo(s);
				}
				static void foo (String s[]) {
				    System.out.println(
				        "b" + (s[0] + s[1] + s[2] + s[3] + s[4] + s[5] + s[6] +\s
				        s[7] + s[8] + s[9] + s[10] + s[11] + s[12] + s[13] +
				        s[14] + s[15] + s[16] + s[17] + s[18] + s[19] +\s
				        s[20] + s[21] + s[22] + s[23] + s[24] + s[25] +\s
				        s[26] + s[27] + s[28] + s[29])
				        );
				}
				}"""
		},
		"baaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if the generated code is OK when leveraging CombinedBinaryExpression
// variant involving a left-deep right expression at the topmost level, with
// a constant high in tree
public void test0014_combined_binary_expression() {
	assertEquals(20, CombinedBinaryExpression.ARITY_MAX_MIN);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				public static void main(String args[]) {
				    final int max = 30;\s
				    String s[] = new String[max];
				    for (int i = 0; i < max; i++) {
				        s[i] = "a";
				    }
				    foo(s);
				}
				static void foo (String s[]) {
				    final String c = "c";
				    System.out.println(
				        "b" +\s
				         (c + c + c + c + c + c + c + c + c + c +\s
				          c + c + c + c + c + c + c + c + c + c +\s
				          c + c + s[0])
				        );
				}
				}"""
		},
		"bcccccccccccccccccccccca");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// check if the generated code is OK when leveraging CombinedBinaryExpression
// variant involving a left-deep right expression at the topmost level, with
// a constant low in tree
public void test0015_combined_binary_expression() {
	assertEquals(20, CombinedBinaryExpression.ARITY_MAX_MIN);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				public static void main(String args[]) {
				    final int max = 30;\s
				    String s[] = new String[max];
				    for (int i = 0; i < max; i++) {
				        s[i] = "a";
				    }
				    foo(s);
				}
				static void foo (String s[]) {
				    final String c = "c";
				    System.out.println(
				        "b" +\s
				         (c + c + c + c + c + c + c + c + c + c +\s
				          c + c + c + c + c + c + c + c + c + c +\s
				          s[0] + s[1] + s[2])
				        );
				}
				}"""
		},
		"bccccccccccccccccccccaaa");
}

// AST implementation - binary expressions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102728
// Adding combined binary expressions - alternate operands
public void test0016_combined_binary_expression() {
	CombinedBinaryExpression.defaultArityMaxStartingValue = 2;
	this.runConformTest(
		"X.java",
		"""
			public class X {
			void foo(int i1, int i2, int i3, int i4) {
			  System.out.println(i1 - i2 + 0 + i3 + 0 + i4);
			}
			}
			""",
		defaultParser,
		new ASTCollector() {
			public boolean visit(BinaryExpression binaryExpression,
					BlockScope scope) {
				super.visit(binaryExpression, scope);
				this.collector.append(binaryExpression);
				return true;
			}
		},
		"(((((i1 - i2) + 0) + i3) + 0) + i4)((((i1 - i2) + 0) + i3) + 0)" +
			"(((i1 - i2) + 0) + i3)((i1 - i2) + 0)(i1 - i2)");
	CombinedBinaryExpression.defaultArityMaxStartingValue =
		CombinedBinaryExpression.ARITY_MAX_MIN;
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157170
public void test0017() {
	CompilerOptions options = new CompilerOptions();
	options.complianceLevel = ClassFileConstants.JDK1_5;
	options.sourceLevel = ClassFileConstants.JDK1_5;
	options.targetJDK = ClassFileConstants.JDK1_5;
	this.runConformTest(
		"X.java",
		"""
			@interface Annot {
				int value() default 0;
			}
			@Annot
			@Annot(3)
			@Annot(value=4)
			public class X {
			}
			""",
		new Parser(
				new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory()), false),
		new AnnotationCollector(),
		"""
			marker annotation start visit
			marker annotation end visit
			single member annotation start visit
			3
			single member annotation end visit
			normal annotation start visit
			member value pair start visit
			value, 4
			member value pair end visit
			normal annotation end visit
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157170
public void test0018() {
	CompilerOptions options = new CompilerOptions();
	options.complianceLevel = ClassFileConstants.JDK1_5;
	options.sourceLevel = ClassFileConstants.JDK1_5;
	options.targetJDK = ClassFileConstants.JDK1_5;
	options.docCommentSupport = true;
	this.runConformTest(
		"X.java",
		"""
			@interface Annot {
				int value() default 0;
			}
			/**
			 * @see Annot
			 */
			@Annot
			@Annot(3)
			@Annot(value=4)
			public class X {
				/**
				 * @see Annot
				 */
				public void foo() {}
			}
			""",
		new Parser(
				new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory()), false),
		new AnnotationCollector(),
		"""
			java doc single type reference start visit
			java doc single type reference end visit
			marker annotation start visit
			marker annotation end visit
			single member annotation start visit
			3
			single member annotation end visit
			normal annotation start visit
			member value pair start visit
			value, 4
			member value pair end visit
			normal annotation end visit
			java doc single type reference start visit
			java doc single type reference end visit
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157170
public void test0019() {
	CompilerOptions options = new CompilerOptions();
	options.complianceLevel = ClassFileConstants.JDK1_5;
	options.sourceLevel = ClassFileConstants.JDK1_5;
	options.targetJDK = ClassFileConstants.JDK1_5;
	options.docCommentSupport = true;
	this.runConformTest(
		"X.java",
		"""
			@interface Annot {
				int value() default 0;
			}
			/**
			 * @see Annot
			 */
			@Annot
			@Annot(3)
			@Annot(value=4)
			public class X {
				/**
				 * @see Annot
				 */
				public void foo(@Annot int i) {
					@Annot int j = 0;\
				}
			}
			""",
		new Parser(
				new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				options,
				new DefaultProblemFactory()), false),
		new AnnotationCollector(),
		"""
			java doc single type reference start visit
			java doc single type reference end visit
			marker annotation start visit
			marker annotation end visit
			single member annotation start visit
			3
			single member annotation end visit
			normal annotation start visit
			member value pair start visit
			value, 4
			member value pair end visit
			normal annotation end visit
			java doc single type reference start visit
			java doc single type reference end visit
			start argument
			marker annotation start visit
			marker annotation end visit
			exit argument
			start local declaration
			marker annotation start visit
			marker annotation end visit
			exit local declaration
			""");
}
}

// Helper classes: define visitors leveraged by some tests
class ASTCollector extends ASTVisitor {
	StringBuilder collector = new StringBuilder();
public String result() {
	return this.collector.toString();
}
}

class ASTBinaryExpressionCollector extends ASTCollector {
static final int LIMIT = 30;
// help limit the output in length by suppressing the middle
// part of strings which length exceeds LIMIT
String cut(String source) {
	int length;
	if ((length = source.length()) > LIMIT) {
		StringBuilder result = new StringBuilder(length);
		result.append(source.substring(0, LIMIT - 10));
		result.append("...");
		result.append(source.substring(length - 7, length));
		return result.toString();
	} else {
		return source;
	}
}
public void endVisit(BinaryExpression binaryExpression, BlockScope scope) {
	this.collector.append("[ev BE " + cut(binaryExpression.toString()) + "]\n");
	super.endVisit(binaryExpression, scope);
}

public void endVisit(CharLiteral charLiteral, BlockScope scope) {
	this.collector.append("[ev CL " + cut(charLiteral.toString()) + "]\n");
	super.endVisit(charLiteral, scope);
}

public void endVisit(ExtendedStringLiteral literal, BlockScope scope) {
	this.collector.append("[ev ESL " + cut(literal.toString()) + "]\n");
	super.endVisit(literal, scope);
}

public void endVisit(SingleNameReference singleNameReference,
		BlockScope scope) {
	this.collector.append("[ev SNR " + cut(singleNameReference.toString()) +
		"]\n");
	super.endVisit(singleNameReference, scope);
}

public void endVisit(StringLiteral stringLiteral, BlockScope scope) {
	this.collector.append("[ev SL " + cut(stringLiteral.toString()) + "]\n");
	super.endVisit(stringLiteral, scope);
}

public void endVisit(StringLiteralConcatenation literal, BlockScope scope) {
	this.collector.append("[ev SLC " + cut(literal.toString()) + "]\n");
	super.endVisit(literal, scope);
}

public boolean visit(BinaryExpression binaryExpression, BlockScope scope) {
	this.collector.append("[v BE " + cut(binaryExpression.toString()) + "]\n");
	return super.visit(binaryExpression, scope);
}

public boolean visit(CharLiteral charLiteral, BlockScope scope) {
	this.collector.append("[v CL " + cut(charLiteral.toString()) + "]\n");
	return super.visit(charLiteral, scope);
}

public boolean visit(ExtendedStringLiteral literal, BlockScope scope) {
	this.collector.append("[v ESL " + cut(literal.toString()) + "]\n");
	return super.visit(literal, scope);
}

public boolean visit(SingleNameReference singleNameReference,
		BlockScope scope) {
	this.collector.append("[v SNR " + cut(singleNameReference.toString()) +
		"]\n");
	return super.visit(singleNameReference, scope);
}

public boolean visit(StringLiteral stringLiteral, BlockScope scope) {
	this.collector.append("[v SL " + cut(stringLiteral.toString()) + "]\n");
	return super.visit(stringLiteral, scope);
}

public boolean visit(StringLiteralConcatenation literal, BlockScope scope) {
	this.collector.append("[v SLC " + cut(literal.toString()) + "]\n");
	return super.visit(literal, scope);
}
}
class AnnotationCollector extends ASTCollector {
public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
	this.collector.append("marker annotation start visit\n");
	return true;
}
public void endVisit(MarkerAnnotation annotation, BlockScope scope) {
	this.collector.append("marker annotation end visit\n");
}
public boolean visit(NormalAnnotation annotation, BlockScope scope) {
	this.collector.append("normal annotation start visit\n");
	return true;
}
public void endVisit(NormalAnnotation annotation, BlockScope scope) {
	this.collector.append("normal annotation end visit\n");
}
public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
	this.collector.append("single member annotation start visit\n");
	this.collector.append(annotation.memberValue.toString());
	this.collector.append("\n");
	return true;
}
public void endVisit(SingleMemberAnnotation annotation, BlockScope scope) {
	this.collector.append("single member annotation end visit\n");
}
public void endVisit(JavadocSingleTypeReference typeRef, BlockScope scope) {
	this.collector.append("java doc single type reference end visit\n");
}
public void endVisit(JavadocSingleTypeReference typeRef, ClassScope scope) {
	this.collector.append("java doc single type reference end visit\n");
}
public boolean visit(JavadocSingleTypeReference typeRef, BlockScope scope) {
	this.collector.append("java doc single type reference start visit\n");
	return true;
}
public boolean visit(JavadocSingleTypeReference typeRef, ClassScope scope) {
	this.collector.append("java doc single type reference start visit\n");
	return true;
}
public boolean visit(MemberValuePair pair, BlockScope scope) {
	this.collector.append("member value pair start visit\n");
	this.collector.append(pair.name);
	this.collector.append(", ");
	this.collector.append(pair.value.toString());
	this.collector.append("\n");
	return true;
}
public void endVisit(MemberValuePair pair, BlockScope scope) {
	this.collector.append("member value pair end visit\n");
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.Argument, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
 */
public void endVisit(Argument argument, BlockScope scope) {
	this.collector.append("exit argument\n");
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.Argument, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
 */
public void endVisit(Argument argument, ClassScope scope) {
	this.collector.append("exit argument\n");
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.LocalDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
 */
public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
	this.collector.append("exit local declaration\n");
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Argument, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
 */
public boolean visit(Argument argument, BlockScope scope) {
	this.collector.append("start argument\n");
	return true;
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.Argument, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
 */
public boolean visit(Argument argument, ClassScope scope) {
	this.collector.append("start argument\n");
	return true;
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.LocalDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
 */
public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
	this.collector.append("start local declaration\n");
	return true;
}
}