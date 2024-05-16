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
 *     Jesper S Moller - realigned with bug 399695
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class ComplianceDiagnoseTest extends AbstractRegressionTest {
	public ComplianceDiagnoseTest(String name) {
		super(name);
	}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test0042" };
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 21, 50 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public static Class testClass() {
	return ComplianceDiagnoseTest.class;
}
public void runComplianceParserTest(
	String[] testFiles,
	String expected13ProblemLog,
	String expected14ProblemLog,
	String expected15ProblemLog){
	if(this.complianceLevel == ClassFileConstants.JDK1_3) {
		this.runNegativeTest(testFiles, expected13ProblemLog);
	} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
		this.runNegativeTest(testFiles, expected14ProblemLog);
	} else if(this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(testFiles, expected15ProblemLog);
	}
}
public void runComplianceParserTest(
		String[] testFiles,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String expected17ProblemLog){
		if(this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel < ClassFileConstants.JDK1_7) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		}
	}
public void runComplianceParserTest(
		String[] testFiles,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String expected16ProblemLog,
		String expected17ProblemLog){
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_5) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_6) {
			this.runNegativeTest(testFiles, expected16ProblemLog);
		} else if(this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		}
	}
public void runComplianceParserTest(
		String[] testFiles,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String expected16ProblemLog,
		String expected17ProblemLog,
		String expected18ProblemLog){
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_5) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_6) {
			this.runNegativeTest(testFiles, expected16ProblemLog);
		} else if(this.complianceLevel < ClassFileConstants.JDK1_8) {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		} else {
			this.runNegativeTest(testFiles, expected18ProblemLog);
		}
	}
public void runComplianceParserTest(
		String[] testFiles,
		String expected1_3ProblemLog,
		String expected1_4ProblemLog,
		String expected1_5ProblemLog,
		String expected1_6ProblemLog,
		String expected1_7ProblemLog,
		String expected1_8ProblemLog,
		String expected9ProblemLog,
		String expected10ProblemLog,
		String expected11ProblemLog,
		String expected12ProblemLog,
		String expected13ProblemLog,
		String expected14ProblemLog,
		String expected15ProblemLog,
		String below16ProblemLog,
		String expected16ProblemLog,
		String expected17ProblemLog,
		String expected18ProblemLog,
		String expected19ProblemLog,
		String expected20ProblemLog,
		String expected22ProblemLog
		){
		if (this.complianceLevel == ClassFileConstants.JDK1_3) {
			this.runNegativeTest(testFiles, expected1_3ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_4) {
			this.runNegativeTest(testFiles, expected1_4ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_5) {
			this.runNegativeTest(testFiles, expected1_5ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_6) {
			this.runNegativeTest(testFiles, expected1_6ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_7) {
			this.runNegativeTest(testFiles, expected1_7ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK1_8) {
			this.runNegativeTest(testFiles, expected1_8ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK9) {
			this.runNegativeTest(testFiles, expected9ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK10) {
			this.runNegativeTest(testFiles, expected10ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK11) {
			this.runNegativeTest(testFiles, expected11ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK12) {
			this.runNegativeTest(testFiles, expected12ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK13) {
			this.runNegativeTest(testFiles, expected13ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK14) {
			this.runNegativeTest(testFiles, expected14ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK15) {
			this.runNegativeTest(testFiles, expected15ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK16) {
			this.runNegativeTest(testFiles, expected16ProblemLog);
		} else if (this.complianceLevel == ClassFileConstants.JDK17) {
			this.runNegativeTest(testFiles, expected17ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK18) {
			this.runNegativeTest(testFiles, expected18ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK19) {
			this.runNegativeTest(testFiles, expected19ProblemLog);
		} else if(this.complianceLevel == ClassFileConstants.JDK20) {
			this.runNegativeTest(testFiles, expected20ProblemLog);
		} else {
			this.runNegativeTest(testFiles, expected22ProblemLog);
		}
	}
public void test0001() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import static aaa.BBB.*;
			public class X {
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.BBB.*;
			^^^^^^^^^^^^^^^^^^^^^^^^
		Syntax error, static imports are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			import static aaa.BBB.*;
			              ^^^
		The import aaa cannot be resolved
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.BBB.*;
			              ^^^
		The import aaa cannot be resolved
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0002() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import static aaa.BBB.CCC;
			public class X {
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.BBB.CCC;
			^^^^^^^^^^^^^^^^^^^^^^^^^^
		Syntax error, static imports are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			import static aaa.BBB.CCC;
			              ^^^
		The import aaa cannot be resolved
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.BBB.CCC;
			              ^^^
		The import aaa cannot be resolved
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
// TODO: Fix this and Enable
public void test0003() {
	String[] testFiles = new String[] {
		"x/X.java", """
			package x;
			public enum X {
			}
			"""
	};

	String expected13ProblemLog = """
		----------
		1. ERROR in x\\X.java (at line 2)
			public enum X {
			       ^^^^
		Syntax error on token "enum", class expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0004() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(){
					for(String o: c) {
					}
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			for(String o: c) {
			    ^^^^^^^^^^^
		Syntax error, 'for each' statements are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 3)
			for(String o: c) {
			              ^
		c cannot be resolved to a variable
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			for(String o: c) {
			              ^
		c cannot be resolved to a variable
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0005() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(Z ... arg){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(Z ... arg){
			         ^^^^^^^^^
		Syntax error, varargs are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 2)
			void foo(Z ... arg){
			         ^
		Z cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(Z ... arg){
			         ^
		Z cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0006() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                                               ^
		Y cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                           ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                                               ^
		Y cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0007() {
	String[] testFiles = new String[] {
		"X.java",
		"public interface X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public interface X <T1 extends String, T2> extends Y {
			                    ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			public interface X <T1 extends String, T2> extends Y {
			                                                   ^
		Y cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 1)
			public interface X <T1 extends String, T2> extends Y {
			                               ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 1)
			public interface X <T1 extends String, T2> extends Y {
			                                                   ^
		Y cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0008() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T1 extends String, T2> int foo(){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extends String, T2> int foo(){
			        ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 2)
			public <T1 extends String, T2> int foo(){
			                   ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 2)
			public <T1 extends String, T2> int foo(){
			                                   ^^^^^
		This method must return a result of type int
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0009() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T1 extends String, T2> X(){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extends String, T2> X(){
			        ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		""";

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 2)
			public <T1 extends String, T2> X(){
			                   ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		""";

	if(this.complianceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(testFiles, expected13ProblemLog);
	} else {
		runConformTest(
			true,
			testFiles,
			expected15ProblemLog,
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
}

public void testPatternsInCase() {
	String[] testFiles = new String[] {
		"X.java",
		"""
		public class X {
		    public static void main(String [] args) {
		        Object o = null;
		        switch (o) {
		            case X x, null:
		                break;
		            case String s, default :
		               break;
		        }
		    }
	    }
		"""
	};

	String expectedProblemLogFrom1_1_6 =
					"""
		----------
		1. ERROR in X.java (at line 4)
			switch (o) {
			        ^
		Cannot switch on a value of type Object. Only convertible int values or enum variables are permitted
		----------
		2. ERROR in X.java (at line 5)
			case X x, null:
			^^^^^^^^^^^^^^
		Multi-constant case labels supported from Java 14 onwards only
		----------
		3. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Type Patterns' is only available with source level 16 and above
		----------
		4. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		5. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		6. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		Cannot mix pattern with other case labels
		----------
		7. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		A null case label has to be either the only expression in a case label or the first expression followed only by a default
		----------
		8. ERROR in X.java (at line 7)
			case String s, default :
			^^^^^^^^^^^^^^^^^^^^^^
		Multi-constant case labels supported from Java 14 onwards only
		----------
		9. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Type Patterns' is only available with source level 16 and above
		----------
		10. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		11. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		12. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		Cannot mix pattern with other case labels
		----------
		13. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
		----------
		""";

	String expectedProblemLogFrom7_13 =
			"""
		----------
		1. ERROR in X.java (at line 4)
			switch (o) {
			        ^
		Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted
		----------
		2. ERROR in X.java (at line 5)
			case X x, null:
			^^^^^^^^^^^^^^
		Multi-constant case labels supported from Java 14 onwards only
		----------
		3. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Type Patterns' is only available with source level 16 and above
		----------
		4. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		5. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		6. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		Cannot mix pattern with other case labels
		----------
		7. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		A null case label has to be either the only expression in a case label or the first expression followed only by a default
		----------
		8. ERROR in X.java (at line 7)
			case String s, default :
			^^^^^^^^^^^^^^^^^^^^^^
		Multi-constant case labels supported from Java 14 onwards only
		----------
		9. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Type Patterns' is only available with source level 16 and above
		----------
		10. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		11. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		12. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		Cannot mix pattern with other case labels
		----------
		13. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
		----------
		""";

	String expectedProblemLogFrom14_15 =
			"""
		----------
		1. ERROR in X.java (at line 4)
			switch (o) {
			        ^
		Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted
		----------
		2. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Type Patterns' is only available with source level 16 and above
		----------
		3. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		4. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		5. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		Cannot mix pattern with other case labels
		----------
		6. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		A null case label has to be either the only expression in a case label or the first expression followed only by a default
		----------
		7. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Type Patterns' is only available with source level 16 and above
		----------
		8. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		9. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		10. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		Cannot mix pattern with other case labels
		----------
		11. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
		----------
		""";

	String expectedProblemLogFrom16_20 =
			"""
		----------
		1. ERROR in X.java (at line 4)
			switch (o) {
			        ^
		Cannot switch on a value of type Object. Only convertible int values, strings or enum variables are permitted
		----------
		2. ERROR in X.java (at line 5)
			case X x, null:
			     ^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		3. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		4. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		Cannot mix pattern with other case labels
		----------
		5. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		A null case label has to be either the only expression in a case label or the first expression followed only by a default
		----------
		6. ERROR in X.java (at line 7)
			case String s, default :
			     ^^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		7. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		The Java feature 'Pattern Matching in Switch' is only available with source level 21 and above
		----------
		8. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		Cannot mix pattern with other case labels
		----------
		9. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
		----------
		""";

	String expectedProblemLogFrom21 =
			"""
		----------
		1. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		Cannot mix pattern with other case labels
		----------
		2. ERROR in X.java (at line 5)
			case X x, null:
			          ^^^^
		A null case label has to be either the only expression in a case label or the first expression followed only by a default
		----------
		3. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		Cannot mix pattern with other case labels
		----------
		4. ERROR in X.java (at line 7)
			case String s, default :
			               ^^^^^^^
		A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
		----------
		""";

	if (this.complianceLevel < ClassFileConstants.JDK1_7) {  // before switching on strings
		runNegativeTest(
			testFiles,
			expectedProblemLogFrom1_1_6);
	}
	else if (this.complianceLevel < ClassFileConstants.JDK14) { // before multi case
		runNegativeTest(
				testFiles,
				expectedProblemLogFrom7_13);
	} else if (this.complianceLevel < ClassFileConstants.JDK16) { // before type patterns
			runNegativeTest(
					testFiles,
					expectedProblemLogFrom14_15);
	} else if (this.complianceLevel < ClassFileConstants.JDK21) { // before case patterns
		runNegativeTest(
				testFiles,
				expectedProblemLogFrom16_20);
	} else {
		runNegativeTest(
				testFiles,
				expectedProblemLogFrom21);
	}
}
public void test0010() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				Z<Y1, Y2> var;
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			  ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		3. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			  ^^
		Y1 cannot be resolved to a type
		----------
		4. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			      ^^
		Y2 cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			  ^^
		Y1 cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			      ^^
		Y2 cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0011() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public X(){
					<Y1, Y2>this(null);
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			<Y1, Y2>this(null);
			 ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 3)
			<Y1, Y2>this(null);
			 ^^
		Y1 cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 3)
			<Y1, Y2>this(null);
			     ^^
		Y2 cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			<Y1, Y2>this(null);
			 ^^
		Y1 cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 3)
			<Y1, Y2>this(null);
			     ^^
		Y2 cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 3)
			<Y1, Y2>this(null);
			        ^^^^^^^^^^^
		The constructor X(null) is undefined
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0012() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
			  void foo() {
			    assert true;
			  }
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 3)
			assert true;
			^^^^^^
		'assert' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on
		----------
		2. ERROR in X.java (at line 3)
			assert true;
			^^^^^^
		Syntax error on token "assert", AssignmentOperator expected after this token
		----------
		""";
	String expected14ProblemLog =
		"";

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0013() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import static aaa.*
			public class X {
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.*
			^^^^^^^^^^^^^^^^^
		Syntax error, static imports are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			import static aaa.*
			              ^^^
		The import aaa cannot be resolved
		----------
		3. ERROR in X.java (at line 1)
			import static aaa.*
			                  ^
		Syntax error on token "*", ; expected after this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.*
			              ^^^
		The import aaa cannot be resolved
		----------
		2. ERROR in X.java (at line 1)
			import static aaa.*
			                  ^
		Syntax error on token "*", ; expected after this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0014() {
	String[] testFiles = new String[] {
		"X.java",
		"public enum X \n" +
		"}\n"
	};

	String expected13ProblemLog = """
		----------
		1. WARNING in X.java (at line 1)
			public enum X\s
			       ^^^^
		'enum' should not be used as an identifier, since it is a reserved keyword from source level 1.5 on
		----------
		2. ERROR in X.java (at line 2)
			}
			^
		Syntax error on token "}", ; expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public enum X\s
			            ^
		Syntax error on token "X", { expected after this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0015() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(){
					for(String o: c) {
						#
					}
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			for(String o: c) {
			    ^^^^^^^^^^^
		Syntax error, 'for each' statements are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0016() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(Z ... arg){
				}
				#
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(Z ... arg){
			         ^^^^^^^^^
		Syntax error, varargs are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 2)
			void foo(Z ... arg){
			         ^
		Z cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(Z ... arg){
			         ^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0017() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X <T1 extends String, T2> extends Y {
				#
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                                               ^
		Y cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                           ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2> extends Y {
			                                               ^
		Y cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0018() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T1 extends String, T2> int foo(){
				}
				#
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extends String, T2> int foo(){
			        ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 2)
			public <T1 extends String, T2> int foo(){
			                   ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0019() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				Z<Y1, Y2> var;
				#
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			  ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		3. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			  ^^
		Y1 cannot be resolved to a type
		----------
		4. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			      ^^
		Y2 cannot be resolved to a type
		----------
		5. ERROR in X.java (at line 3)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			  ^^
		Y1 cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			Z<Y1, Y2> var;
			      ^^
		Y2 cannot be resolved to a type
		----------
		4. ERROR in X.java (at line 3)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0020() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
			  void foo() {
			    assert true;
			    #
			  }
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 3)
			assert true;
			^^^^^^
		'assert' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on
		----------
		2. ERROR in X.java (at line 3)
			assert true;
			^^^^^^
		Syntax error on token "assert", AssignmentOperator expected after this token
		----------
		3. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";
	String expected14ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 4)
			#
			^
		Syntax error on token "Invalid Character", delete this token
		----------
		""";

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//TODO (david) suspicious behavior
public void test0021() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import staic aaa.*;
			public class X {
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import staic aaa.*;
			       ^^^^^
		The import staic cannot be resolved
		----------
		2. ERROR in X.java (at line 1)
			import staic aaa.*;
			             ^^^
		Syntax error on token "aaa", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import staic aaa.*;
			       ^^^^^
		Syntax error on token "staic", static expected
		----------
		2. ERROR in X.java (at line 1)
			import staic aaa.*;
			       ^^^^^
		The import staic cannot be resolved
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//TODO (david) suspicious behavior
public void test0022() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import static aaa.*.*;
			public class X {
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.*.*;
			^^^^^^^^^^^^^^^^^
		Syntax error, static imports are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 1)
			import static aaa.*.*;
			              ^^^
		The import aaa cannot be resolved
		----------
		3. ERROR in X.java (at line 1)
			import static aaa.*.*;
			                   ^^
		Syntax error on tokens, delete these tokens
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static aaa.*.*;
			              ^^^
		The import aaa cannot be resolved
		----------
		2. ERROR in X.java (at line 1)
			import static aaa.*.*;
			                  ^
		Syntax error on token "*", Identifier expected
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0023() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import static for;
			public class X {
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static for;
			       ^^^^^^^^^^
		Syntax error on tokens, Name expected instead
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 1)
			import static for;
			              ^^^
		Syntax error on token "for", invalid Name
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}

//TODO (david) reenable once bug is fixed
public void _test0024() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			import static {aaa};
			public class X {
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static {aaa};
			       ^^^^^^^^^^^^
		Syntax error on tokens, Name expected instead
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			import static {aaa};
			              ^^^^^
		Syntax error on tokens, Name expected instead
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0025() {
	String[] testFiles = new String[] {
		"x/X.java", """
			package x;
			static aaa.*;
			public class X {
			}

			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in x\\X.java (at line 2)
			static aaa.*;
			^^^^^^
		Syntax error on token "static", import expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in x\\X.java (at line 1)
			package x;
			         ^
		Syntax error on token ";", import expected after this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0026() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(){
					for(Object o ? c){
					}
				}
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			for(Object o ? c){
			    ^^^^^^
		Syntax error on token "Object", ( expected
		----------
		2. ERROR in X.java (at line 3)
			for(Object o ? c){
			           ^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		3. ERROR in X.java (at line 3)
			for(Object o ? c){
			                ^
		Syntax error, insert "AssignmentOperator Expression" to complete Assignment
		----------
		4. ERROR in X.java (at line 3)
			for(Object o ? c){
			                ^
		Syntax error, insert "; ; ) Statement" to complete BlockStatements
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			for(Object o ? c){
			             ^
		Syntax error on token "?", : expected
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0027() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(){
					for(Object o : switch){
					}
				}
			}
			
			"""
	};

	String expected13ProblemLog =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for(Object o : switch){\n" +
			"	             ^\n" +
			"Syntax error on token \":\", delete this token\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	for(Object o : switch){\n" +
			"	             ^\n" +
			"Syntax error, insert \": Expression )\" to complete EnhancedForStatementHeader\n" +  // FIXME: bogus suggestion, this rule is compliance 1.5
			"----------\n" +
			"3. ERROR in X.java (at line 3)\n" +
			"	for(Object o : switch){\n" +
			"	             ^\n" +
			"Syntax error, insert \"Statement\" to complete BlockStatements\n" +
			"----------\n";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			for(Object o : switch){
			               ^^^^^^
		Syntax error on token "switch", invalid Expression
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0028() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(int ... ){
				}
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int ... ){
			             ^^^
		Syntax error on token "...", invalid VariableDeclaratorId
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int ... ){
			             ^^^
		Syntax error on token "...", VariableDeclaratorId expected after this token
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0029() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(int ... for){
				}
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int ... for){
			             ^^^^^^^
		Syntax error on tokens, VariableDeclaratorId expected instead
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int ... for){
			                 ^^^
		Syntax error on token "for", invalid VariableDeclaratorId
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void _test0030() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(int .. aaa){
				}
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int .. aaa){
			             ^^
		Syntax error on tokens, delete these tokens
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int .. aaa){
			             ^^
		Syntax error on tokens, delete these tokens
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0031() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(int ... aaa bbb){
				}
			}
			
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int ... aaa bbb){
			         ^^^^^^^^^^^
		Syntax error, varargs are only available if source level is 1.5 or greater
		----------
		3. ERROR in X.java (at line 2)
			void foo(int ... aaa bbb){
			             ^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		4. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X {
			               ^
		Syntax error, insert "}" to complete ClassBody
		----------
		2. ERROR in X.java (at line 2)
			void foo(int ... aaa bbb){
			                     ^^^
		Syntax error on token "bbb", delete this token
		----------
		3. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void _test0032() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X <T1 extends String, T2 extends Y {
			\t
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2 extends Y {
			               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2 extends Y {
			                                              ^
		Y cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 1)
			public class X <T1 extends String, T2 extends Y {
			                           ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2 extends Y {
			                                              ^
		Syntax error, insert ">" to complete ReferenceType1
		----------
		3. ERROR in X.java (at line 1)
			public class X <T1 extends String, T2 extends Y {
			                                              ^
		Y cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0033() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X T1 extends String, T2> extends Y {
			\t
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X T1 extends String, T2> extends Y {
			               ^^
		Syntax error on token "T1", delete this token
		----------
		2. ERROR in X.java (at line 1)
			public class X T1 extends String, T2> extends Y {
			                          ^^^^^^^^^^^^^^^^^^^
		Syntax error on tokens, delete these tokens
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X T1 extends String, T2> extends Y {
			             ^
		Syntax error on token "X", < expected after this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0034() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X <T1 extnds String, T2> extends Y {
			\t
			}
			"""
	};

	String expected13ProblemLog = """
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			^^^^^^^^^^^^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			                   ^^^^^^^^^^^^^^^^^^
		Syntax error on tokens, ClassHeaderName expected instead
		----------
		3. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			                   ^^^^^^
		extnds cannot be resolved to a type
		----------
		""";

	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			                   ^^^^^^
		Syntax error on token "extnds", extends expected
		----------
		2. ERROR in X.java (at line 1)
			public class X <T1 extnds String, T2> extends Y {
			                   ^^^^^^
		extnds cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0035() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X <T1 extends for, T2> extends Y {
			\t
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extends for, T2> extends Y {
			               ^^^^^^^^^^^^^^^^^^^^
		Syntax error on tokens, delete these tokens
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X <T1 extends for, T2> extends Y {
			                           ^^^
		Syntax error on token "for", invalid ReferenceType
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0036() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T1 extends String, T2> foo(){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extends String, T2> foo(){
			        ^^^^^^^^^^^^^^^^^^^^^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 2)
			public <T1 extends String, T2> foo(){
			                               ^^^^^
		Return type for the method is missing
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 2)
			public <T1 extends String, T2> foo(){
			                   ^^^^^^
		The type parameter T1 should not be bounded by the final type String. Final types cannot be further extended
		----------
		2. ERROR in X.java (at line 2)
			public <T1 extends String, T2> foo(){
			                               ^^^^^
		Return type for the method is missing
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0037() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T1 extnds String, T2> int foo(){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extnds String, T2> int foo(){
			       ^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in X.java (at line 2)
			public <T1 extnds String, T2> int foo(){
			        ^^
		T1 cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			public <T1 extnds String, T2> int foo(){
			                            ^
		Syntax error on token ">", ; expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extnds String, T2> int foo(){
			        ^^
		T1 cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			public <T1 extnds String, T2> int foo(){
			           ^^^^^^
		Syntax error on token "extnds", extends expected
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0038() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T1 extends String T2> int foo(){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extends String T2> int foo(){
			       ^^^^^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in X.java (at line 2)
			public <T1 extends String T2> int foo(){
			                            ^
		Syntax error on token ">", ; expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T1 extends String T2> int foo(){
			                          ^^
		Syntax error on token "T2", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0039() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				Z Y1, Y2> var;
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z Y1, Y2> var;
			^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			Z Y1, Y2> var;
			        ^
		Syntax error on token ">", , expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z Y1, Y2> var;
			^
		Z cannot be resolved to a type
		----------
		2. ERROR in X.java (at line 2)
			Z Y1, Y2> var;
			        ^
		Syntax error on token ">", , expected
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0040() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				Z <Y1, Y2 var;
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z <Y1, Y2 var;
			  ^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in X.java (at line 2)
			Z <Y1, Y2 var;
			       ^^
		Y2 cannot be resolved to a type
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z <Y1, Y2 var;
			       ^^
		Syntax error, insert ">" to complete ReferenceType1
		----------
		2. ERROR in X.java (at line 2)
			Z <Y1, Y2 var;
			       ^^
		Y2 cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0041() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				Z <Y1, for Y2> var;
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z <Y1, for Y2> var;
			  ^^^^^^^^^^^^
		Syntax error on tokens, delete these tokens
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			Z <Y1, for Y2> var;
			       ^^^
		Syntax error on token "for", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0042() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			void ___eval() {
				new Runnable() {
					int ___run() throws Throwable {
						return blah;
					}
					private String blarg;
					public void run() {
					}
				};
			}
			public class X {
				private static int x;
				private String blah;
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
			"""
	};

	String problemLog = (this.complianceLevel >= ClassFileConstants.JDK22) ?
			"""
			----------
			1. ERROR in X.java (at line 1)
				void ___eval() {
				^
			Implicitly Declared Classes and Instance Main Methods is a preview feature and disabled by default. Use --enable-preview to enable
			----------
			2. ERROR in X.java (at line 1)
				void ___eval() {
				^
			Implicitly declared class must have a candidate main method
			----------
			3. ERROR in X.java (at line 4)
				return blah;
				       ^^^^
			blah cannot be resolved to a variable
			----------
			""" :
			"""
			----------
			1. ERROR in X.java (at line 1)
				void ___eval() {
				^
			The preview feature Implicitly Declared Classes and Instance Main Methods is only available with source level 22 and above
			----------
			2. ERROR in X.java (at line 1)
				void ___eval() {
				^
			Implicitly declared class must have a candidate main method
			----------
			3. ERROR in X.java (at line 4)
				return blah;
				       ^^^^
			blah cannot be resolved to a variable
			----------
			""";

	if (this.complianceLevel < ClassFileConstants.JDK16) {
		problemLog += """
			4. ERROR in X.java (at line 14)
				public static void main(String[] args) {
				                   ^^^^^^^^^^^^^^^^^^^
			The method main cannot be declared static; static methods can only be declared in a static or top level type
			----------
			""";
	}
	runNegativeTest(testFiles, problemLog);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72942
 */
public void test0043() {
	String[] testFiles = new String[] {
		"x/X.java", """
			package x;
			public class X {
			}
			public static void foo(){}

			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in x\\X.java (at line 3)
			}
			^
		Syntax error on token "}", delete this token
		----------
		2. ERROR in x\\X.java (at line 4)
			public static void foo(){}
			                         ^
		Syntax error, insert "}" to complete ClassBody
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected13ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62472
 */
public void test0044() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public <T> X(T t){
					System.out.println(t);
				}
				}
				public static void main(String[] args) {
					class Local extends X {
						Local() {
							<String>super("SUCCESS");
						}
					}
					new Local();
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			public <T> X(T t){
			        ^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 5)
			}
			^
		Syntax error on token "}", delete this token
		----------
		3. ERROR in X.java (at line 9)
			<String>super("SUCCESS");
			 ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 5)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=62472
 */
public void test0045() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public void foo(){
				}
				}
				public void bar() {
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 4)
			}
			^
		Syntax error on token "}", delete this token
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected13ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74519
 */
public void test0046() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public @interface X {
				String annName();
			}"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			public @interface X {
			                  ^
		Syntax error, annotation declarations are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog = "";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74519
 */
public void test0047() {
	String[] testFiles = new String[] {
		"A.java",
		"public @interface A {}",
		"X.java",
		"@A public class X {\n" +
		"}"
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in A.java (at line 1)
			public @interface A {}
			                  ^
		Syntax error, annotation declarations are only available if source level is 1.5 or greater
		----------
		----------
		1. ERROR in X.java (at line 1)
			@A public class X {
			^^
		Syntax error, annotations are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog = "";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0048() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(X ... arg[]){
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(X ... arg[]){
			         ^^^^^^^^^
		Syntax error, varargs are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(X ... arg[]){
			               ^^^
		Extended dimensions are illegal for a variable argument
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0049() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			@interface MyAnn {
				String value1() default "";
				String value2();
			}
			class ZZZ {}	\t
			public @MyAnn("","") class Test {	\t
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 1)
			@interface MyAnn {
			           ^^^^^
		Syntax error, annotation declarations are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 6)
			public @MyAnn("","") class Test {	\t
			              ^^
		Syntax error, insert ")" to complete Modifier
		----------
		3. ERROR in X.java (at line 6)
			public @MyAnn("","") class Test {	\t
			              ^^
		The attribute value is undefined for the annotation type MyAnn
		----------
		4. ERROR in X.java (at line 6)
			public @MyAnn("","") class Test {	\t
			                           ^^^^
		The public type Test must be defined in its own file
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;
	String token = (this.complianceLevel >= ClassFileConstants.JDK21) ? "." : "<";
	String expected15ProblemLog =
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	              ^^\n" +
		"The attribute value is undefined for the annotation type MyAnn\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	                ^\n" +
		"Syntax error on token \",\", "+ token +" expected\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	public @MyAnn(\"\",\"\") class Test {		\n" +
		"	                           ^^^^\n" +
		"The public type Test must be defined in its own file\n" +
		"----------\n";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0050() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(List<String>... args) {}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(List<String>... args) {}
			         ^^^^^^^^^^^^^^^^^^^^
		Syntax error, varargs are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 2)
			void foo(List<String>... args) {}
			         ^^^^
		List cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			void foo(List<String>... args) {}
			              ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(List<String>... args) {}
			         ^^^^
		List cannot be resolved to a type
		----------
		""";
	String expected17ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(List<String>... args) {}
			         ^^^^
		List cannot be resolved to a type
		----------
		2. WARNING in X.java (at line 2)
			void foo(List<String>... args) {}
			                         ^^^^
		Type safety: Potential heap pollution via varargs parameter args
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog,
		expected17ProblemLog
	);
}
public void test0051() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void foo(java.util.List2<String>... args) {}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(java.util.List2<String>... args) {}
			         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Syntax error, varargs are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 2)
			void foo(java.util.List2<String>... args) {}
			         ^^^^^^^^^^^^^^^
		java.util.List2 cannot be resolved to a type
		----------
		3. ERROR in X.java (at line 2)
			void foo(java.util.List2<String>... args) {}
			                         ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(java.util.List2<String>... args) {}
			         ^^^^^^^^^^^^^^^
		java.util.List2 cannot be resolved to a type
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154811
public void test0052() {
	String[] testFiles = new String[] {
		"X.java",
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
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 5)
			void foo2() {
			^^^^
		Syntax error on token "void", new expected
		----------
		2. ERROR in X.java (at line 7)
			class Z<T> {\s
			^^^^^
		Syntax error on token "class", invalid AssignmentOperator
		----------
		3. ERROR in X.java (at line 7)
			class Z<T> {\s
			         ^
		Syntax error on token ">", ; expected
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 5)
			void foo2() {
			^^^^
		Syntax error on token "void", new expected
		----------
		2. ERROR in X.java (at line 6)
			}
			^
		Syntax error, insert ";" to complete Statement
		----------
		""";

	String expectedJ14ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 5)
			void foo2() {
			^^^^
		Syntax error on token "void", record expected
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		(this.complianceLevel < ClassFileConstants.JDK14 ? expected15ProblemLog : expectedJ14ProblemLog)
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=42243
public void test0053() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					assert true;
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. WARNING in X.java (at line 3)
			assert true;
			^^^^^^
		'assert' should not be used as an identifier, since it is a reserved keyword from source level 1.4 on
		----------
		2. ERROR in X.java (at line 3)
			assert true;
			^^^^^^
		Syntax error on token "assert", AssignmentOperator expected after this token
		----------
		""";
	String expected14ProblemLog =
		"";

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
public void test0054() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					try (int i = 0) {};
				}
			}
			"""
	};

	String expected13ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			try (int i = 0) {};
			     ^^^^^^^^^
		Resource specification not allowed here for source level below 1.7
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected14ProblemLog;

	String expected17ProblemLog =
		"""
		----------
		1. ERROR in X.java (at line 3)
			try (int i = 0) {};
			     ^^^
		The resource type int does not implement java.lang.AutoCloseable
		----------
		""";
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog,
		expected17ProblemLog
	);
}
// test that use of multi-catch is flagged accordingly
public void test0055() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_7) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			import java.io.*;
			public class X {
				public static void main(String[] args) {
					try {
						System.out.println();
						Reader r = new FileReader(args[0]);
						r.read();
					} catch(IOException | RuntimeException e) {
						e.printStackTrace();
					}
				}
			}
			"""
	};

	String expected13ProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 8)
			} catch(IOException | RuntimeException e) {
			        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Multi-catch parameters are not allowed for source level below 1.7
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected14ProblemLog;

	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog
	);
}
// rethrow should not be precisely computed in 1.6-
public void test0056() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public static void main(String[] args) {
					try {
						throw new DaughterOfFoo();
					} catch(Foo e) {
						try {
							throw e;
						} catch (SonOfFoo e1) {
						 	e1.printStackTrace();
						} catch (Foo e1) {}
					}
				}
			}
			class Foo extends Exception {}
			class SonOfFoo extends Foo {}
			class DaughterOfFoo extends Foo {}
			"""
	};

	String expected13ProblemLog =
			"""
		----------
		1. WARNING in X.java (at line 14)
			class Foo extends Exception {}
			      ^^^
		The serializable class Foo does not declare a static final serialVersionUID field of type long
		----------
		2. WARNING in X.java (at line 15)
			class SonOfFoo extends Foo {}
			      ^^^^^^^^
		The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
		----------
		3. WARNING in X.java (at line 16)
			class DaughterOfFoo extends Foo {}
			      ^^^^^^^^^^^^^
		The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
		----------
		""";
	String expected14ProblemLog =
		expected13ProblemLog;

	String expected15ProblemLog =
		expected14ProblemLog;

	String expected17ProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 8)
			} catch (SonOfFoo e1) {
			         ^^^^^^^^
		Unreachable catch block for SonOfFoo. This exception is never thrown from the try statement body
		----------
		2. WARNING in X.java (at line 14)
			class Foo extends Exception {}
			      ^^^
		The serializable class Foo does not declare a static final serialVersionUID field of type long
		----------
		3. WARNING in X.java (at line 15)
			class SonOfFoo extends Foo {}
			      ^^^^^^^^
		The serializable class SonOfFoo does not declare a static final serialVersionUID field of type long
		----------
		4. WARNING in X.java (at line 16)
			class DaughterOfFoo extends Foo {}
			      ^^^^^^^^^^^^^
		The serializable class DaughterOfFoo does not declare a static final serialVersionUID field of type long
		----------
		""";
	runComplianceParserTest(
		testFiles,
		expected13ProblemLog,
		expected14ProblemLog,
		expected15ProblemLog,
		expected17ProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0057() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  public default void foo() { System.out.println(); }
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 2)
			public default void foo() { System.out.println(); }
			                    ^^^^^
		Default methods are allowed only at source level 1.8 or above
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0058() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  void foo(int p);
			}
			public class X {
			  I i = System::exit;
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 5)
			I i = System::exit;
			      ^^^^^^^^^^^^
		Method references are allowed only at source level 1.8 or above
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0059() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  void foo(int p);
			}
			class Y {
			   static void goo(int x) {
			   }
			}
			public class X extends Y {
			  I i = super::goo;
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 9)
			I i = super::goo;
			      ^^^^^^^^^^
		Method references are allowed only at source level 1.8 or above
		----------
		2. ERROR in X.java (at line 9)
			I i = super::goo;
			      ^^^^^^^^^^
		The method goo(int) from the type Y should be accessed in a static way\s
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0060() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  void foo(int p);
			}
			class Y {
			   void goo(int x) {
			   }
			}
			public class X extends Y {
			  I i = new Y()::goo;
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 9)
			I i = new Y()::goo;
			      ^^^^^^^^^^^^
		Method references are allowed only at source level 1.8 or above
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0061() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  void foo(int p);
			}
			class Y {
			   void goo(int x) {
			   }
			   Y() {}
			   Y(int x) {}
			}
			public class X extends Y {
			  I i = Y::new;
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 11)
			I i = Y::new;
			      ^^^^^^
		Constructor references are allowed only at source level 1.8 or above
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383714
public void test0062() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  int foo(int p);
			}
			public class X {
			  I i = p -> 10 + 20 + 30;
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 5)
			I i = p -> 10 + 20 + 30;
			      ^^^^^^^^^^^^^^^^^
		Lambda expressions are allowed only at source level 1.8 or above
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381358
public void test0063() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4 || this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
			  int foo(int p);
			}
			public class X<T> {
			  I i = X<String>::foo;
			  I i2 = (p) -> 10;
			  public static int foo(int p) {
				return p;
			  }
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 5)
			I i = X<String>::foo;
			      ^^^^^^^^^^^^^^
		Method references are allowed only at source level 1.8 or above
		----------
		2. ERROR in X.java (at line 5)
			I i = X<String>::foo;
			      ^^^^^^^^^^^^^^
		The method foo(int) from the type X<String> should be accessed in a static way\s
		----------
		3. ERROR in X.java (at line 6)
			I i2 = (p) -> 10;
			       ^^^^^^^^^
		Lambda expressions are allowed only at source level 1.8 or above
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383913#c22
public void test0064() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] source = new String[] {
		"X.java",
		"""
			class X {
				void foo(X this){}
			}"""
	};
	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 2)
			void foo(X this){}
			           ^^^^
		Explicit declaration of 'this' parameter is allowed only at source level 1.8 or above
		----------
		""";
	runComplianceParserTest(
			source,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391201
public void testBug391201() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8 || this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				@Marker int foo(@Marker int p) {
					@Marker int i = 0;
					return i;
				}
				@Marker
				class Y {}
				@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
				@interface Marker {}\
			}""",
		"java/lang/annotation/ElementType.java",
		"""
			package java.lang.annotation;
			public enum ElementType {
			    TYPE,
			    FIELD,
			    METHOD,
			    PARAMETER,
			    CONSTRUCTOR,
			    LOCAL_VARIABLE,
			    ANNOTATION_TYPE,
			    PACKAGE,
			    TYPE_PARAMETER,
			    TYPE_USE
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 2)
			@Marker int foo(@Marker int p) {
			^^^^^^^
		Syntax error, type annotations are available only when source level is at least 1.8
		----------
		2. ERROR in X.java (at line 2)
			@Marker int foo(@Marker int p) {
			                ^^^^^^^
		Syntax error, type annotations are available only when source level is at least 1.8
		----------
		3. ERROR in X.java (at line 3)
			@Marker int i = 0;
			^^^^^^^
		Syntax error, type annotations are available only when source level is at least 1.8
		----------
		4. ERROR in X.java (at line 6)
			@Marker
			^^^^^^^
		Syntax error, type annotations are available only when source level is at least 1.8
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
public void testBug399773() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_8)
		return;
	String[] testFiles = new String[] {
		"X.java",
		"""
			interface I {
				void doit();
				default void doitalso () {}
			}
			interface J {
				void doit();
				default void doitalso () {}
			}
			public class X {
				Object p = (I & J) () -> {};
			}
			""" ,
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 3)
			default void doitalso () {}
			             ^^^^^^^^^^^
		Default methods are allowed only at source level 1.8 or above
		----------
		2. ERROR in X.java (at line 7)
			default void doitalso () {}
			             ^^^^^^^^^^^
		Default methods are allowed only at source level 1.8 or above
		----------
		3. ERROR in X.java (at line 10)
			Object p = (I & J) () -> {};
			            ^^^^^
		Additional bounds are not allowed in cast operator at source levels below 1.8
		----------
		4. ERROR in X.java (at line 10)
			Object p = (I & J) () -> {};
			                   ^^^^^
		Lambda expressions are allowed only at source level 1.8 or above
		----------
		5. ERROR in X.java (at line 10)
			Object p = (I & J) () -> {};
			                   ^^^^^
		The target type of this expression must be a functional interface
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778,  [1.8][compiler] Conditional operator expressions should propagate target types
public void testBug399778() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	String[] testFiles = new String[] {
		"X.java",
		"""
			import java.util.Arrays;
			import java.util.List;
			public class X  {
					List<String> l = null == null ? Arrays.asList() : Arrays.asList("Hello","world");
			}
			""",
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 4)
			List<String> l = null == null ? Arrays.asList() : Arrays.asList("Hello","world");
			                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Type mismatch: cannot convert from List<capture#1-of ? extends Object> to List<String>
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		""   // 1.8 should compile this fine.
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399778,  [1.8][compiler] Conditional operator expressions should propagate target types
public void testBug399778a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	String[] testFiles = new String[] {
		"X.java",
		"""
			import java.util.Arrays;
			import java.util.List;
			public class X  {
					List<String> l = (List<String>) (null == null ? Arrays.asList() : Arrays.asList("Hello","world"));
			}
			""",
	};

	String expectedProblemLog =
			"""
		----------
		1. WARNING in X.java (at line 4)
			List<String> l = (List<String>) (null == null ? Arrays.asList() : Arrays.asList("Hello","world"));
			                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Type safety: Unchecked cast from List<capture#1-of ? extends Object> to List<String>
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog   // 1.8 also issue type safety warning.
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399780: static methods in interfaces.
public void testBug399780() {
	if(this.complianceLevel >= ClassFileConstants.JDK1_8) {
		return;
	}
	String[] testFiles = new String[] {
		"I.java",
		"""
			interface I {
			  public static void foo1() { System.out.println(); }
			  public static void foo2();
			  public abstract static void foo3();
			}
			"""
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in I.java (at line 2)
			public static void foo1() { System.out.println(); }
			                   ^^^^^^
		Static methods are allowed in interfaces only at source level 1.8 or above
		----------
		2. ERROR in I.java (at line 2)
			public static void foo1() { System.out.println(); }
			                   ^^^^^^
		Illegal modifier for the interface method foo1; only public & abstract are permitted
		----------
		3. ERROR in I.java (at line 3)
			public static void foo2();
			                   ^^^^^^
		Illegal modifier for the interface method foo2; only public & abstract are permitted
		----------
		4. ERROR in I.java (at line 4)
			public abstract static void foo3();
			                            ^^^^^^
		Illegal modifier for the interface method foo3; only public & abstract are permitted
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=399769:  Use of '_' as identifier name should trigger a diagnostic
public void testBug399781() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
			   int _;
				void foo(){
					int _   = 3;
					int _123 = 4;
					int a_   = 5;
				}
			   void goo(int _) {}
			   void zoo() {
			      try {
			      } catch (Exception _) {
			      }
			   }
			}
			""",
	};
	String problemLog = null;
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		problemLog = """
						----------
						1. WARNING in X.java (at line 4)
							int _   = 3;
							    ^
						The local variable _ is hiding a field from type X
						----------
						2. WARNING in X.java (at line 8)
							void goo(int _) {}
							             ^
						The parameter _ is hiding a field from type X
						----------
						3. WARNING in X.java (at line 11)
							} catch (Exception _) {
							                   ^
						The parameter _ is hiding a field from type X
						----------
						""";
	} else if (this.complianceLevel == ClassFileConstants.JDK1_8) {
		problemLog = """
					----------
					1. WARNING in X.java (at line 2)
						int _;
						    ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					2. WARNING in X.java (at line 4)
						int _   = 3;
						    ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					3. WARNING in X.java (at line 4)
						int _   = 3;
						    ^
					The local variable _ is hiding a field from type X
					----------
					4. WARNING in X.java (at line 8)
						void goo(int _) {}
						             ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					5. WARNING in X.java (at line 8)
						void goo(int _) {}
						             ^
					The parameter _ is hiding a field from type X
					----------
					6. WARNING in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
					----------
					7. WARNING in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					The parameter _ is hiding a field from type X
					----------
					""";
	} else if (this.complianceLevel < ClassFileConstants.JDK22) {
		problemLog = """
					----------
					1. ERROR in X.java (at line 2)
						int _;
						    ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					2. ERROR in X.java (at line 4)
						int _   = 3;
						    ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					3. WARNING in X.java (at line 4)
						int _   = 3;
						    ^
					The local variable _ is hiding a field from type X
					----------
					4. ERROR in X.java (at line 8)
						void goo(int _) {}
						             ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					5. WARNING in X.java (at line 8)
						void goo(int _) {}
						             ^
					The parameter _ is hiding a field from type X
					----------
					6. ERROR in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					'_' is a keyword from source level 9 onwards, cannot be used as identifier
					----------
					7. WARNING in X.java (at line 11)
						} catch (Exception _) {
						                   ^
					The parameter _ is hiding a field from type X
					----------
					""";
	} else {
		problemLog = """
				----------
				1. ERROR in X.java (at line 2)
					int _;
					    ^
				As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters
				----------
				2. ERROR in X.java (at line 8)
					void goo(int _) {}
					             ^
				As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters
				----------
				3. WARNING in X.java (at line 8)
					void goo(int _) {}
					             ^
				The parameter _ is hiding a field from type X
				----------
				""";
	}
//	(this.complianceLevel < ClassFileConstants.JDK22) ? "" : "";
	runNegativeTest(testFiles, problemLog);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406846:  [1.8] compiler NPE for method reference/lambda code compiled with < 1.8 compliance
public void test406846() {

	if (this.complianceLevel >= ClassFileConstants.JDK1_8) // tested in LET.
		return;

	String[] testFiles = new String[] {
		"X.java",
		"""
			import java.util.*;
			public class X {
			  public static <E> void printItem(E value, int index) {
			    String output = String.format("%d -> %s", index, value);
			    System.out.println(output);
			  }
			  public static void main(String[] argv) {
			    List<String> list = Arrays.asList("A","B","C");
			    eachWithIndex(list,X::printItem);
			  }
			  interface ItemWithIndexVisitor<E> {
			    public void visit(E item, int index);
			  }
			  public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {
			    for (int i = 0; i < list.size(); i++) {
			         visitor.visit(list.get(i), i);
			    }
			  }
			}
			""",
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 9)
			eachWithIndex(list,X::printItem);
			                   ^^^^^^^^^^^^
		Method references are allowed only at source level 1.8 or above
		----------
		""";
	String expected1314ProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 3)
			public static <E> void printItem(E value, int index) {
			               ^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		2. ERROR in X.java (at line 4)
			String output = String.format("%d -> %s", index, value);
			                       ^^^^^^
		The method format(String, Object[]) in the type String is not applicable for the arguments (String, int, E)
		----------
		3. ERROR in X.java (at line 8)
			List<String> list = Arrays.asList("A","B","C");
			     ^^^^^^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		4. ERROR in X.java (at line 8)
			List<String> list = Arrays.asList("A","B","C");
			                           ^^^^^^
		The method asList(T[]) in the type Arrays is not applicable for the arguments (String, String, String)
		----------
		5. ERROR in X.java (at line 9)
			eachWithIndex(list,X::printItem);
			                   ^^^^^^^^^^^^
		Method references are allowed only at source level 1.8 or above
		----------
		6. ERROR in X.java (at line 11)
			interface ItemWithIndexVisitor<E> {
			                               ^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		7. ERROR in X.java (at line 14)
			public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {
			               ^
		Syntax error, type parameters are only available if source level is 1.5 or greater
		----------
		8. ERROR in X.java (at line 14)
			public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {
			                                          ^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		9. ERROR in X.java (at line 14)
			public static <E> void eachWithIndex(List<E> list, ItemWithIndexVisitor<E> visitor) {
			                                                                        ^
		Syntax error, parameterized types are only available if source level is 1.5 or greater
		----------
		""";

	runComplianceParserTest(
			testFiles,
			expected1314ProblemLog,
			expected1314ProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog,
			expectedProblemLog
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=401850: [1.8][compiler] Compiler fails to type poly allocation expressions in method invocation contexts
// FAIL: sub-optimal overload picked
public void test401850() {

	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					   static void foo(Object o) {
						   System.out.println("foo(Object)");
					   }
					   static void foo(X<String> o) {
						   System.out.println("foo(X<String>)");
					   }
					   public static void main(String[] args) {\s
					      foo(new X<>());\s
					   }\s
					}
					""",
			},
			this.complianceLevel == ClassFileConstants.JDK1_7 ? "foo(Object)" : "foo(X<String>)");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429110: [1.8][quick fix] Hovering over the error does not show the quick fix
//FAIL: sub-optimal overload picked
public void test429110() {
	if (this.complianceLevel != ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
			new String[] {
				"java/lang/annotation/ElementType.java",
				"""
					package java.lang.annotation;
					public enum ElementType {
					    TYPE,
					    FIELD,
					    METHOD,
					    PARAMETER,
					    CONSTRUCTOR,
					    LOCAL_VARIABLE,
					    ANNOTATION_TYPE,
					    PACKAGE,
					    TYPE_PARAMETER,
					    TYPE_USE
					}
					""",
				"X.java",
				"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Target;
					import java.util.List;
					public class X {
						@Target(ElementType.TYPE_USE)
						static @interface NonNull { }
						List<@NonNull String> foo(List<@NonNull String> arg) {
							return arg;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					List<@NonNull String> foo(List<@NonNull String> arg) {
					     ^^^^^^^^
				Syntax error, type annotations are available only when source level is at least 1.8
				----------
				2. ERROR in X.java (at line 7)
					List<@NonNull String> foo(List<@NonNull String> arg) {
					                               ^^^^^^^^
				Syntax error, type annotations are available only when source level is at least 1.8
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=421477: [1.8][compiler] strange error message for default method in class
public void test421477() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  default void f() {
					  }
					  default X() {}
					}""",
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	default void f() {\n" +
			"	             ^^^\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_8 ?
			"Default methods are allowed only in interfaces.\n" :
			"Illegal modifier for the method f; only public, protected, private, abstract, static, final, synchronized, native & strictfp are permitted\n")	+
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	default X() {}\n" +
			"	        ^\n" +
			"Syntax error on token \"X\", Identifier expected after this token\n" +
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=428605: [1.8] Error highlighting can be improved for default methods
public void test428605() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X {
					       default void f() {
					       }
					       static void g() {
					       }
					}\s
					"""
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"""
				----------
				1. ERROR in X.java (at line 2)
					default void f() {
					             ^^^
				Default methods are allowed only at source level 1.8 or above
				----------
				2. ERROR in X.java (at line 4)
					static void g() {
					            ^^^
				Static methods are allowed in interfaces only at source level 1.8 or above
				----------
				3. ERROR in X.java (at line 4)
					static void g() {
					            ^^^
				Illegal modifier for the interface method g; only public & abstract are permitted
				----------
				""" :
			""));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440285
// [1.8] Compiler allows array creation reference with type arguments
public void testBug440285() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runNegativeTest(new String [] {
		"X.java",
		"""
			import java.util.function.Function;
			class Y{}
			class Z{}
			public class X {
				Function<Integer, int[]> m1 = int[]::<Y, Z>new;
				Function<Integer, int[]> m2 = int[]::<Y>new;
			}""",},
		"""
			----------
			1. ERROR in X.java (at line 5)
				Function<Integer, int[]> m1 = int[]::<Y, Z>new;
				                                      ^^^^
			Type arguments are not allowed here
			----------
			2. ERROR in X.java (at line 6)
				Function<Integer, int[]> m2 = int[]::<Y>new;
				                                      ^
			Type arguments are not allowed here
			----------
			""");
}
public void testBug531714_001() {
	if (this.complianceLevel >= ClassFileConstants.JDK12)
		return;
	String[] testFiles = 			new String[] {
			"X.java",
			"""
				public class X {
					static int twice(int i) {
						int tw = switch (i) {
							case 0 -> i * 0;
							case 1 -> 2;
							default -> 3;
						};
						return tw;
					}
					public static void main(String[] args) {
						System.out.print(twice(3));
					}
				}
				""",
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 3)
			int tw = switch (i) {
					case 0 -> i * 0;
					case 1 -> 2;
					default -> 3;
				};
			         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Switch Expressions are supported from Java 14 onwards only
		----------
		2. ERROR in X.java (at line 4)
			case 0 -> i * 0;
			^^^^^^
		Arrow in case statement supported from Java 14 onwards only
		----------
		3. ERROR in X.java (at line 5)
			case 1 -> 2;
			^^^^^^
		Arrow in case statement supported from Java 14 onwards only
		----------
		4. ERROR in X.java (at line 6)
			default -> 3;
			^^^^^^^
		Arrow in case statement supported from Java 14 onwards only
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
public void testBug531714_002() {
	if (this.complianceLevel >= ClassFileConstants.JDK12)
		return;
	String[] testFiles = new String[] {
			"X.java",
			"""
				public class X {
					static int twice(int i) {
						switch (i) {
							case 0 -> i * 0;
							case 1 -> 2;
							default -> 3;
						}
						return 0;
					}
					public static void main(String[] args) {
						System.out.print(twice(3));
					}
				}
				""",
	};

	String expectedProblemLog =
			"""
		----------
		1. ERROR in X.java (at line 4)
			case 0 -> i * 0;
			^^^^^^
		Arrow in case statement supported from Java 14 onwards only
		----------
		2. ERROR in X.java (at line 5)
			case 1 -> 2;
			^^^^^^
		Arrow in case statement supported from Java 14 onwards only
		----------
		3. ERROR in X.java (at line 6)
			default -> 3;
			^^^^^^^
		Arrow in case statement supported from Java 14 onwards only
		----------
		""";

	runComplianceParserTest(
		testFiles,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog,
		expectedProblemLog
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2008
// Support for identifier '_' for old compile source/target versions
public void testIssue2008() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public X(){
				}
			   void _() {
			       _();
			   }
			       public static void main(String [] args) {
			           System.out.println("OK");
			       }
			   class _ {
			   }
			}
			"""
	};

	String expectedProblemLogUpto1_7 = "";
	String expected1_8ProblemLog = """
			----------
			1. WARNING in X.java (at line 4)
				void _() {
				     ^
			'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
			----------
			2. WARNING in X.java (at line 5)
				_();
				^
			'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
			----------
			3. WARNING in X.java (at line 10)
				class _ {
				      ^
			'_' should not be used as an identifier, since it is a reserved keyword from source level 1.8 on
			----------
			""";
	String expected9to20ProblemLog = """
			----------
			1. ERROR in X.java (at line 4)
				void _() {
				     ^
			'_' is a keyword from source level 9 onwards, cannot be used as identifier
			----------
			2. ERROR in X.java (at line 5)
				_();
				^
			'_' is a keyword from source level 9 onwards, cannot be used as identifier
			----------
			3. ERROR in X.java (at line 10)
				class _ {
				      ^
			'_' is a keyword from source level 9 onwards, cannot be used as identifier
			----------
			""";

	String expected22ProblemLog = """
					----------
					1. ERROR in X.java (at line 4)
						void _() {
						     ^
					Syntax error on token "_", Identifier expected
					----------
					2. ERROR in X.java (at line 4)
						void _() {
						     ^
					void is an invalid type for the variable _
					----------
					3. ERROR in X.java (at line 5)
						_();
						^
					Syntax error on token "_", this expected
					----------
					4. ERROR in X.java (at line 10)
						class _ {
						      ^
					Syntax error on token "_", Identifier expected
					----------\n""";

	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTest(
			true,
			testFiles,
			expectedProblemLogUpto1_7,
			"OK", null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	} else if(this.complianceLevel == ClassFileConstants.JDK1_8) {
		runConformTest(
				true,
				testFiles,
				expected1_8ProblemLog,
				"OK", null,
				JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	} else if(this.complianceLevel < ClassFileConstants.JDK22) {
		runNegativeTest(
				testFiles,
				expected9to20ProblemLog);
	} else {
		runNegativeTest(
				testFiles,
				expected22ProblemLog);
	}
}
}
