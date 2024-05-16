/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     						bug 325755 - [compiler] wrong initialization state after conditional expression
 *     						bug 133125 - [compiler][null] need to report the null status of expressions and analyze them simultaneously
 *     						bug 292478 - Report potentially null across variable assignment
 *     						bug 319201 - [null] no warning when unboxing SingleNameReference causes NPE
 *     						bug 320170 - [compiler] [null] Whitebox issues in null analysis
 *     						bug 332637 - Dead Code detection removing code that isn't dead
 *     						bug 338303 - Warning about Redundant assignment conflicts with definite assignment
 *     						bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
 * 							bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
 * 							bug 354554 - [null] conditional with redundant condition yields weak error message
 * 							bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
 * 							bug 349326 - [1.7] new warning for missing try-with-resources
 * 							bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
 * 							bug 367879 - Incorrect "Potential null pointer access" warning on statement after try-with-resources within try-finally
 * 							bug 383690 - [compiler] location of error re uninitialized final field should be aligned
 *							bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *							bug 376263 - Bogus "Potential null pointer access" warning
 *							bug 331649 - [compiler][null] consider null annotations for fields
 *							bug 382789 - [compiler][null] warn when syntactically-nonnull expression is compared against null
 *							bug 401088 - [compiler][null] Wrong warning "Redundant null check" inside nested try statement
 *							bug 401092 - [compiler][null] Wrong warning "Redundant null check" in outer catch of nested try
 *							bug 400761 - [compiler][null] null may be return as boolean without a diagnostic
 *							bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
 *							bug 403147 - [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
 *							bug 384380 - False positive on a "Potential null pointer access" after a continue
 *							bug 406384 - Internal error with I20130413
 *							Bug 364326 - [compiler][null] NullPointerException is not found by compiler. FindBugs finds that one
 *							Bug 453483 - [compiler][null][loop] Improve null analysis for loops
 *							Bug 195638 - [compiler][null][refactoring] Wrong error : "Null pointer access: The variable xxx can only be null at this location " with try..catch in loop
 *							Bug 454031 - [compiler][null][loop] bug in null analysis; wrong "dead code" detection
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/* See also NullReferenceImplTests for low level, implementation dependent
 * tests. */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullReferenceTest extends AbstractRegressionTest {

public NullReferenceTest(String name) {
	super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "testBug542707_1" };
//		TESTS_NAMES = new String[] { "testBug384380" };
//		TESTS_NAMES = new String[] { "testBug384380_b" };
//		TESTS_NAMES = new String[] { "testBug321926a2" };
//		TESTS_NAMES = new String[] { "testBug432109" };
//		TESTS_NAMES = new String[] { "testBug418500" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return NullReferenceTest.class;
}

// Conditionally augment problem detection settings
static boolean setNullRelatedOptions = true;
@Override
protected Map getCompilerOptions() {
    Map defaultOptions = super.getCompilerOptions();
    if (setNullRelatedOptions) {
	    defaultOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	    defaultOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
		defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.ENABLED);
    }
    return defaultOptions;
}

protected void runNegativeNullTest(String[] testFiles, String expectedCompilerLog) {
	runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- simple case for local
public void test0001_simple_local() {
	runNegativeTest(
		new String[] {
			"X.java",
			  """
				public class X {
				  void foo() {
				    Object o = null;
				    o.toString();
				  }
				}
				"""},
	    """
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- simple case for field
// the current design leaves fields out of the analysis altogether
public void test0002_simple_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo() {
				    o = null;
				    o.toString();
				  }
				}
				"""},
	""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- simple case for parameter
public void test0003_simple_parameter() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    o = null;
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- final local
public void test0004_final_local() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    final Object o = null;
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- final local
public void test0005_final_local() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    final Object o;
				    o.toString();
				  }
				}
				"""},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"The local variable o may not have been initialized\n" +
			// hides the null related message, but complains once, which is good
		"----------\n");
}

// null analysis -- final local
public void test0006_final_local() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    final Object o = null;\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) { /* */ }
				    ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o != null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- local with member
public void test0007_local_with_member() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo() {\n" +
			"    X x = null;\n" +
			"    x.m.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				x.m.toString();
				^
			Null pointer access: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- local with member
public void test0008_local_with_member() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo() {\n" +
			"    X x = null;\n" +
			"    System.out.println(x.m);\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				System.out.println(x.m);
				                   ^
			Null pointer access: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- local with member
public void test0009_local_with_member() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m;\n" +
			"  void foo(X x) {\n" +
			"    x.m.toString();\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0010_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    bar();\n" + // defuses null by side effect
			"    o.toString();\n" +
			"  }\n" +
			"  void bar() {\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0011_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  static Object o;\n" +
			"  void foo() {\n" +
			"    o = null;\n" +
			"    bar();\n" + // defuses null by side effect
			"    o.toString();\n" +
			"  }\n" +
			"  static void bar() {\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- field
public void test0012_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo() {
				    o = null;
				    bar();
				    o.toString();
				  }
				  static void bar() {
				  }
				}
				"""},
		"" // still ok because the class may hold a pointer to this
	);
}

// null analysis -- field
public void test0013_field_with_method_call() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  static Object o;
				  void foo() {
				    o = null;
				    bar();
				    o.toString();
				  }
				  void bar() {
				  }
				}
				"""},
		"" // still ok because this may place a static call upon X
	);
}

// null analysis -- field
public void test0014_field_with_explicit_this_access() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo() {
				    o = null;
				    this.o.toString();
				  }
				}
				"""},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	this.o.toString();\n" +
//      "	^^^^^^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0015_field_with_explicit_this_access() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo() {
				    this.o = null;
				    o.toString();
				  }
				}
				"""},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0016_field_of_another_object() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo() {
				    X other = new X();
				    other.o = null;
				    other.o.toString();
				  }
				}
				"""},
		"");
}

// null analysis -- field
public void test0017_field_of_another_object() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo() {
				    X other = this;
				    o = null;
				    other.o.toString();
				  }
				}
				"""},
		"");
}

// null analysis -- field
public void test0018_field_of_enclosing_object() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object o;\n" +
			"  public class Y {\n" +
			"    void foo() {\n" +
			"      X.this.o = null;\n" +
			"      X.this.o.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"}\n"},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 6)\n" +
//      "	X.this.o.toString();\n" +
//      "	^^^^^^^^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- fields
// check that fields that are protected against concurrent access
// behave as locals when no call to further methods can affect them
public void test0019_field_synchronized() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  public synchronized void foo() {
				    o = null;
				    o.toString();
				  }
				  void bar() {/* */}
				}
				"""},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
// check that final fields behave as locals despite calls to further
// methods
public void test0020_final_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  final Object o = null;
				  public synchronized void foo() {
				    bar();
				    o.toString();
				  }
				  void bar() {/* */}
				}
				"""},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0021_final_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  final Object o = null;
				  X () {
				    bar();
				    o.toString();
				  }
				  void bar() {/* */}
				}
				"""},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	o.toString();\n" +
//      "	^\n" +
//      "The field o is likely null; it was either set to null or checked for null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0022_final_field() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  final Object o = new Object();
				  X () {
				    bar();
				    if (o == null) { /* empty */ }
				  }
				  void bar() {/* */}
				}
				"""},
		""
//      "----------\n" +
//      "1. ERROR in X.java (at line 5)\n" +
//      "	if (o == null) { /* empty */ }\n" +
//      "	    ^\n" +
//      "The field o is likely non null; it was either set to a non-null value or assumed to be non-null when last used\n" +
//      "----------\n"
	);
}

// null analysis -- field
public void test0023_field_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m;
				  void foo(X x) {
				    Object o = x.m;
				    if (o == null) { /* */ };
				  }
				}
				"""},
		"");
}

// null analysis -- field
public void test0024_field_cast_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m;
				  void foo(Object x) {
				    Object o = ((X) x).m;
				    if (o == null) { /* */ };
				  }
				}
				"""},
		"");
}

// null analysis -- parameter
public void test0025_parameter() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" + // quiet: parameters have unknown value
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- suppress warnings
public void test0026_suppress_warnings() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					@SuppressWarnings("null")
					public class X {
					  void foo() {
					    Object o = null;
					    o.toString();
					  }
					}
					"""},
		    "", null, true, null, compilerOptions, null);
	}
}

// null analysis -- embedded comparison
public void test0027_embedded_comparison() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    if (b) { /* */ }\n" +
			"    o.toString();\n" + 		// complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- field
public void test0028_field_as_initializer() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  X f;
				  void foo() {
				    X x = f;
				    if (x == null) { /* */ }
				  }
				}
				"""},
		"");
}

// null analysis -- field
public void test0029_field_assignment() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m;
				  void foo() {
				    X x = null;
				    x.m = new Object();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				x.m = new Object();
				^
			Null pointer access: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
public void test0030_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = true ? null : null;
				    o.toString();
				  }
				}
				"""},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Object o = true ? null : null;
					                         ^^^^
				Dead code
				----------
				2. ERROR in X.java (at line 4)
					o.toString();
					^
				Null pointer access: The variable o can only be null at this location
				----------
				""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
public void test0031_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = true ? null : new Object();
				    o.toString();
				  }
				}
				"""},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Object o = true ? null : new Object();
					                         ^^^^^^^^^^^^
				Dead code
				----------
				2. ERROR in X.java (at line 4)
					o.toString();
					^
				Null pointer access: The variable o can only be null at this location
				----------
				""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
public void test0032_conditional_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = false ? null : new Object();
				    o.toString();
				  }
				}
				"""},
		"");
}

// null analysis -- conditional expression
public void test0033_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = (1 == 1) ? null : new Object();
				    o.toString();
				  }
				}
				"""},
			"""
				----------
				1. WARNING in X.java (at line 3)
					Object o = (1 == 1) ? null : new Object();
					           ^^^^^^^^
				Comparing identical expressions
				----------
				2. WARNING in X.java (at line 3)
					Object o = (1 == 1) ? null : new Object();
					                             ^^^^^^^^^^^^
				Dead code
				----------
				3. ERROR in X.java (at line 4)
					o.toString();
					^
				Null pointer access: The variable o can only be null at this location
				----------
				""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
public void test0034_conditional_expression() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo() {
				    Object o = b ? null : new Object();
				    o.toString();
				  }
				}
				"""},
			"""
				----------
				1. ERROR in X.java (at line 5)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
// variant with constant condition
public void test0034_conditional_expression_2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo() {
				    Object o = false ? null : new Object();
				    o.toString();
				  }
				}
				"""},
			"");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
public void test0034_conditional_expression_3() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo(Object a) {
				 	 if (a == null) {}
				    Object o = b ? a : new Object();
				    o.toString();
				  }
				}
				"""},
			"""
				----------
				1. ERROR in X.java (at line 6)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
// variant with dependency between condition and expression - LocalDeclaration
// TODO(stephan) cannot analyse this flow dependency
public void _test0034_conditional_expression_4() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object u) {\n" +
			"    if (u == null) {}\n" + //taint
			"    Object o = (u == null) ? new Object() : u;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"");
}

// null analysis -- conditional expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133125
// variant with dependency between condition and expression - Assignment
// TODO(stephan) cannot analyse this flow dependency
public void _test0034_conditional_expression_5() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object u) {\n" +
			"    if (u == null) {}\n" + //taint
			"    Object o;\n" +
			"    o = (u == null) ? new Object() : u;\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"");
}

// null analysis -- conditional expression
public void test0035_conditional_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo() {
				    Object o = b ? null : new Object();
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"");
}

// null analysis -- conditional expression
public void test0036_conditional_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo() {
				    Object o = b ? null : null;
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null) { /* */ }
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test0037_conditional_expression_1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					boolean badFunction(int i) {
						return i > 0 ? true : null;
					}
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 3)
				return i > 0 ? true : null;
				       ^^^^^^^^^^^^^^^^^^^
			Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test0037_conditional_expression_2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					int badFunction(int i) {
						return i > 0 ? null : Integer.MIN_VALUE;
					}
					@SuppressWarnings("null")
					int silent(int i) {
						return i > 0 ? null : Integer.MIN_VALUE;
					}
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				return i > 0 ? null : Integer.MIN_VALUE;
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test0037_conditional_expression_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					boolean badFunction3(int i) {
						//expected a potential null problem:
						return i > 0 ? true : (Boolean) null;
					}
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 4)
				return i > 0 ? true : (Boolean) null;
				                      ^^^^^^^^^^^^^^
			Null pointer access: This expression of type Boolean is null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
// if-then-else instead of conditional expression
public void test0037_conditional_expression_4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // needs autoboxing
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_UNNECESSARY_ELSE, JavaCore.IGNORE);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					boolean badFunction4(int i) {
					if (i > 0)
						return true;
					else
						// expected a null problem:
						return (Boolean) null;
					}
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 7)
				return (Boolean) null;
				       ^^^^^^^^^^^^^^
			Null pointer access: This expression of type Boolean is null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
// pot-null cond-expr in receiver position
public void test0037_conditional_expression_5() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					String badFunction3(int i) {
						return (i > 0 ? this : null).toString();
					}
					String badFunction4(int i) {
						Object o = null;
						return (i > 0 ? o : null).toString();
					}
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 3)
				return (i > 0 ? this : null).toString();
				       ^^^^^^^^^^^^^^^^^^^^^
			Potential null pointer access: This expression may be null
			----------
			2. ERROR in X.java (at line 7)
				return (i > 0 ? o : null).toString();
				       ^^^^^^^^^^^^^^^^^^
			Null pointer access: This expression can only be null
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// finally block injects pot-nn into itself via enclosing loop
public void test0037_autounboxing_1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					void foo1(boolean b) {
				       int j = 0;
				       Integer i = null;
				       while (true) {
				           try {
				               j = 1;
				           } finally {
				               j = (b?i:1)+1;
				               i = 2;
				           }
				       }
				   }
					void foo2(boolean b) {
				       int j = 0;
				       Integer i = null;
				       try {
				           j = 1;
				       } finally {
				           j = (b?i:1)+1;
				           i = 2;
				       }
				   }
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 9)
				j = (b?i:1)+1;
				       ^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			2. ERROR in X.java (at line 20)
				j = (b?i:1)+1;
				       ^
			Null pointer access: This expression of type Integer is null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// inject pot.nn from try into finally
public void test0037_autounboxing_2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					void foo2(boolean b) {
				       int j = 0;
				       Integer i = null;
				       while (true) {
				           try {
				               if (b)
				                   i = 3;
				           } finally {
				               j = (b?i:1)+1;
				           }
				       }
				   }
					void foo3(boolean b) {
				       int j = 0;
				       Integer i = null;
				       try {
				           if (b)
				               i = 3;
				       } finally {
				           j = (b?i:1)+1;
				       }
				   }
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 10)
				j = (b?i:1)+1;
				       ^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			2. ERROR in X.java (at line 21)
				j = (b?i:1)+1;
				       ^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// null from try, nn from catch, merge both into finally
public void test0037_autounboxing_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					void foo3(Integer i, boolean b) {
				       int j = 0;
				       while (true) {
				           try {
				               i = null;
				               unsafe();
				           } catch (Exception e) {
				               i = 3;
				           } finally {
				               j = (b?i:1)+1;
				           }
				       }
				   }
					void foo4(Integer i, boolean b) {
				       int j = 0;
				       try {
				           i = null;
				           unsafe();
				       } catch (Exception e) {
				           i = 3;
				       } finally {
				           while (j < 0)
				               j = (b?i:1)+1;
				       }
				   }
				
				   private void unsafe() throws Exception {
				        throw new Exception();
				   }
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 11)
				j = (b?i:1)+1;
				       ^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			2. ERROR in X.java (at line 24)
				j = (b?i:1)+1;
				       ^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// effective protection locally within the finally block
public void test0037_autounboxing_4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo3(Integer i, boolean b) {
				       int j = 0;
				       while (true) {
				           try {
				               i = null;
				               unsafe();
				           } catch (Exception e) {
				               i = 3;
				           } finally {
								if (i == null) i = 4;
				               j = (b?i:1)+1;
				           }
				       }
				   }
					void foo4(Integer i, boolean b) {
				       int j = 0;
				       try {
				           i = null;
				           unsafe();
				       } catch (Exception e) {
				           i = 3;
				       } finally {
				           while (i == null)
								i = 4;
				           while (j < 4)
				               j = (b?i:1)+1;
				       }
				   }
				
				   private void unsafe() throws Exception {
				        throw new Exception();
				   }
				}
				"""},
		options);
}
// https://bugs.eclipse.org/403147 [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
// array reference in nested try
public void test0037_autounboxing_5() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
						void foo(Object [] o, boolean b, Integer i) {
						int j = 1;
						try {
							if (b) i = null;
						} catch (RuntimeException r) {
							i = 3;
						} finally {
							try {
								System.out.println(o[i]); \s
							} finally {
								System.out.println(j);
							}
						}
					}
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 10)
				System.out.println(o[i]); \s
				                     ^
			Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// Bug 406384 - Internal error with I20130413
public void test0037_autounboxing_6() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					void test(List<String> l1, List<String> l2, int i, Object val) {
						for (String s1 : l1) {
							for (String s2 : l2) {
								switch (i) {
								case 1:\s
									boolean booleanValue = (Boolean)val;
								}
							}
						}
					}
				}
				"""
		});
}

// null analysis -- autoboxing
public void test0040_autoboxing_compound_assignment() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo() {
					    Integer i = null;
					    i += 1;
					  }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 4)
					i += 1;
					^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
public void test0041_autoboxing_increment_operator() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = null;\n" +
				"    i++;\n" + // complain: this is null
				"    ++i;\n" + // quiet (because previous step guards it)
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					i++;
					^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
public void test0042_autoboxing_literal() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = 0;\n" +
				"    if (i == null) {};\n" + // complain: this is non null
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (i == null) {};
					    ^
				Null comparison always yields false: The variable i cannot be null at this location
				----------
				2. WARNING in X.java (at line 4)
					if (i == null) {};
					               ^^
				Dead code
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
public void test0043_autoboxing_literal() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Integer i = null;\n" +
				"    System.out.println(i + 4);\n" + // complain: this is null
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					System.out.println(i + 4);
					                   ^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- autoboxing
// origin: AssignmentTest#test020
public void test0044_autoboxing() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    int i = 0;
				    boolean b = i < 10;
				  }
				}
				"""},
		"");
}

// null analysis -- autoboxing
// variant of 42 for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165346
public void test0045_autoboxing_operator() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo() {
					    int j = 5;\
					    Integer i = 0 + j;
					    if (i == null) {}
					  }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (i == null) {}
					    ^
				Null comparison always yields false: The variable i cannot be null at this location
				----------
				2. WARNING in X.java (at line 4)
					if (i == null) {}
					               ^^
				Dead code
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- array
public void test0050_array() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    args = new String[] {\"zero\"};\n" +
			"    args[0] = null;\n" +
			"    if (args[0] == null) {};\n" +
			     // quiet: we don't keep track of all array elements
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- array
public void test0051_array() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public static void main(String args[]) {\n" +
			"    args = null;\n" +
			"    args[0].toString();\n" + // complain: args is null
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				args[0].toString();
				^^^^
			Null pointer access: The variable args can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- array
public void test0052_array() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(String args[]) {\n" +
			"    String s = args[0];\n" +
			"    if (s == null) {};\n" +
			     // quiet: we don't keep track of all array elements
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- array
public void test0053_array() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo(String args[]) {
				    for (int i = 0; i < args.length; i++) { /* */}
				  }
				}
				"""},
		"");
}

// null analysis -- method call
public void test0061_method_call_guard() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +      // guards o from being null
			"    if (o == null) {};\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) {};
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null) {};
				               ^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0062_method_call_isolation() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (bar(o = null)) {\n" +
			"      if (o == null) {/* empty */}\n" + // complain
			"    }\n" +
			"  }\n" +
			"  boolean bar(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) {/* empty */}
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0063_method_call_isolation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (bar(o == null ? new Object() : o)) {\n" +
			"      if (o == null) {/* empty */}\n" + // quiet
			"    }\n" +
			"  }\n" +
			"  boolean bar(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - method call
public void test0064_method_call_isolation() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (bar(o = new Object())) {\n" +
			"      if (o == null) {/* empty */}\n" + // complain
			"    }\n" +
			"  }\n" +
			"  boolean bar(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) {/* empty */}
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null) {/* empty */}
				               ^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0065_method_call_invocation_target() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    (o = new Object()).toString();\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - method call
public void test0066_method_call_invocation_target() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    (o = null).toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				(o = null).toString();
				^^^^^^^^^^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0067_method_call_invocation_target() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    (o = new Object()).toString();\n" + // quiet
			"    if (o == null)  { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null)  { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null)  { /* */ }
				                ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - method call
public void test0068_method_call_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  void foo(X x) {\n" +
			"    x = x.bar();\n" +
			"    if (x == null)  { /* */ }\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- type reference
public void test0070_type_reference() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    Class c = java.lang.Object.class;
				    if (c == null) {};
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (c == null) {};
				    ^
			Null comparison always yields false: The variable c cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (c == null) {};
				               ^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test0080_shortcut_boolean_expressions() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o1, Object o2) {
				    if (o1 != null && (o2 = o1) != null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (o1 != null && (o2 = o1) != null) { /* */ }
				                  ^^^^^^^^^
			Redundant null check: The variable o2 cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test0081_shortcut_boolean_expressions() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o1, Object o2) {
				    while (o1 != null && (o2 = o1) != null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 3)
				while (o1 != null && (o2 = o1) != null) { /* */ }
				                     ^^^^^^^^^
			Redundant null check: The variable o2 cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
public void test0082_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    if (o == null || o == null) {
				      o = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (o == null || o == null) {
				                 ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. ERROR in X.java (at line 6)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			3. WARNING in X.java (at line 6)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
public void test0083_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    if (o == null && o == null) {
				      o = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (o == null && o == null) {
				                 ^
			Redundant null check: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 6)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			3. WARNING in X.java (at line 6)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130311
public void test0084_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean foo(Integer i1, Integer i2) {
				    return (i1 == null && i2 == null)
				      || (i1.byteValue() == i2.byteValue());
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				|| (i1.byteValue() == i2.byteValue());
				    ^^
			Potential null pointer access: The variable i1 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130311
public void test0085_shortcut_boolean_expression() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean foo(Integer i1, Integer i2) {
				    return (i1 == null & i2 == null)
				      || (i1.byteValue() == i2.byteValue());
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				|| (i1.byteValue() == i2.byteValue());
				    ^^
			Potential null pointer access: The variable i1 may be null at this location
			----------
			2. ERROR in X.java (at line 4)
				|| (i1.byteValue() == i2.byteValue());
				                      ^^
			Potential null pointer access: The variable i2 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - shortcut boolean expression and correlation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195774
public void test0086_shortcut_boolean_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static int foo(Integer i, Integer j) {
				    if (i == null && j == null) {
				      return 1;
				    }
				    if (i == null) {
				      return j.intValue();
				    }
				    if (j == null) {
				      return i.intValue();
				    }
				    return 0;
				  }
				}"""},
		"");
}

// null analysis - shortcut boolean expression and correlation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195774
public void _test0087_shortcut_boolean_expression() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static int foo(Integer i, Integer j) {
				    if (i == null && j == null) {
				      return 1;
				    }
				    if (j == null) {
				      return i.intValue();
				    }
				    if (i == null) {
				      return j.intValue();
				    }
				    return 0;
				  }
				}"""},
		"");
}

// null analysis -- instanceof
// JLS: instanceof returns false if o turns out to be null
public void test0090_instanceof() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo (Object o) {
					if (dummy) {
					  o = null;
					}
					if (o instanceof X) { /* */ }
				  }
				}"""},
		"");
}

// null analysis -- instanceof
public void test0091_instanceof() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo (Object o) {
					if (dummy) {
					  o = null;
					}
					if (o instanceof X) { /* */ }
					if (o == null) { /* */ }
				  }
				}"""},
		"");
}

// null analysis -- instanceof
// can only be null always yields false
public void test0092_instanceof() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo () {
					Object o = null;
					if (o instanceof X) { /* */ }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o instanceof X) { /* */ }
				    ^
			instanceof always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- instanceof
public void test0093_instanceof() {
	runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object x) {\n" +
			"    if (x instanceof X) {\n" +
			"      if (x == null) { /* */ }\n" + // cannot happen
			"    }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (x == null) { /* */ }
				    ^
			Null comparison always yields false: The variable x cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (x == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- instanceof
public void test0094_instanceof() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object x) {\n" +
			"    if (x instanceof X) {\n" +
			"      return;\n" +
			"    }\n" +
			"    if (x != null) { /* */ }\n" +
			// cannot decide: could be null of new Object() for example
			"  }\n" +
			"}"},
		"");
}

// null analysis -- instanceof combined with conditional or
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=145202
public void test0095_instanceof_conditional_or() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(Object x) {
				    if (! (x instanceof String)
				         || x == null) {
				      return;
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				|| x == null) {
				   ^
			Null comparison always yields false: The variable x cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- strings concatenation
// JLS 15.18.1: if one of the operands is null, it is replaced by "null"
// Note: having the diagnostic could come handy when the initialization path
//       is non trivial; to get the diagnostic, simply put in place an
//       extraneous call to toString() -- and remove it before releasing.
public void test0120_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  String foo(String s1, String s2) {
				    if (s1 == null) { /* */ };
				    return s1 + s2;
				  }
				}
				"""},
		"");
}

// null analysis -- strings concatenation
public void test0121_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  String foo(String s1, String s2) {
				    if (s1 == null) { /* */ };
				    s1 += s2;
				    return s1;
				  }
				}
				"""},
		"");
}

// null analysis -- strings concatenation
public void test0122_strings_concatenation() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  String foo(String s1) {
				    if (s1 == null) { /* */ };
				    return s1.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				return s1.toString();
				       ^^
			Potential null pointer access: The variable s1 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127919
// it should suffice that the return type is String to avoid
// errors
public void test0123_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s, Object o, Integer i) {\n" +
			"    if (s == null || o == null || i == null) { /* */ };\n" +
			"    if (bar()) {\n" +
			"      return s + i;\n" + // quiet: i replaced by "null" if null
			"    }\n" +
			"    return o + s;\n" + // quiet: o replaced by "null" if null
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127919
// variant
public void test0124_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  String foo(String s, Object o, Integer i) {\n" +
			"    if (s == null || o == null || i == null) { /* */ };\n" +
			"    s += o;\n" + // quiet: o replaced by "null" if null
			"    s += i;\n" + // quiet: i replaced by "null" if null
			"    return s;\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127919
// variant
public void test0125_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, Integer i) {\n" +
			"    System.out.println(o + (o == null ? \"\" : o.toString()));\n" + // quiet: o replaced by "null" if null
			"    System.out.println(i + (i == null ? \"\" : i.toString()));\n" + // quiet: o replaced by "null" if null
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132867
public void test0126_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    System.out.println(o + "");
				    if (o != null) { /* */ };
				  }
				}
				"""},
		"");
}

// null analysis -- strings concatenation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132867
public void test0127_strings_concatenation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    System.out.println(o + "");
				  }
				}
				"""},
		"");
}

// null analysis -- if/else
// check that obviously unreachable code does not modify the null
// status of a local
// the said code is not marked as unreachable per JLS 14.21 (the rationale
// being the accommodation for the if (constant_flag_evaluating_to_false)
// {code...} volontary code exclusion pattern)
public void test0300_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() {\n" +
			"    Object o = null;\n" +
			"    if (false) {\n" +
			"      o = new Object();\n" + // skipped
			"    }\n" +
			"    if (true) {\n" +
			"      //\n" +
			"    }\n" +
			"    else {\n" +
			"      o = new Object();\n" + // skipped
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
			"""
				----------
				1. WARNING in X.java (at line 4)
					if (false) {
				      o = new Object();
				    }
					           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				2. WARNING in X.java (at line 10)
					else {
				      o = new Object();
				    }
					     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				3. ERROR in X.java (at line 13)
					o.toString();
					^
				Null pointer access: The variable o can only be null at this location
				----------
				""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0301_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = new Object();
				    if (o != null) {
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0302_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) throws Exception {\n" +
			"    if (o == null) {\n" +
			"      throw new Exception();\n" +
			"    }\n" +
			"    if (o != null) {\n" + // only get there if o non null
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0303_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    if (o == null) {
				      return;
				    }
				    if (o != null) {
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0304_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    if (o == null) {
				      o.toString();
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0305_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    if (o == null) {
				      // do nothing
				    }
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0306_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o.toString().equals(\"\")) {\n" +
			"      if (o == null) {\n" + // complain: could not get here
			"        // do nothing\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) {
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null) {
			        // do nothing
			      }
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0307_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o ==  null) {\n" +
			"      System.exit(0);\n" +
			"    }\n" +
			"    if (o == null) {\n" +
			  // quiet
			  // a direct call to System.exit() can be recognized as such; yet,
			  // a lot of other methods may have the same property (aka calling
			  // System.exit() themselves.)
			"      // do nothing\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - if/else
public void test0308_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b;\n" +
			"  void foo(Object o) {\n" +
			"    if (b) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0309_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b1, b2;\n" +
			"  void foo(Object o) {\n" +
			"    if (b1) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    if (b2) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 10)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0310_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean b1, b2;\n" +
			"  void foo(Object o) {\n" +
			"    if (b1) {\n" +
			"      o = null;\n" +
			"    }\n" +
			"    if (b2) {\n" +
			"      o.toString();\n" + // complain
			"      o.toString();\n" + // silent
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 8)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			2. ERROR in X.java (at line 11)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0311_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null)\n" +
			"      o = new Object();\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"	},
		"");
}

// null analysis - if/else
public void test0312_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"    if (o != null) { /* */ }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 5)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 6)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0313_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o == null) {\n" + // quiet
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			// complain: o set to non null iff it was null
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 6)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0314_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" + // quiet
			"      o = null;\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) { /* */ }
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0315_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" + // quiet
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0316_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o == null || b) { /* */ }\n" + // quiet
			"    else { /* */ }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0317_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o != null) {\n" + // quiet
			"      if (b) {\n" + // quiet
			"        o = null;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0318_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o != null) {\n" + // quiet
			"      if (b) {\n" + // quiet
			"        o = null;\n" +
			"      }\n" +
			"      if (o == null) { /* */ }\n" + // quiet
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
// we do nothing to diagnose the contents of fake reachable code
public void test0319_if_else_dead_branch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (false) {\n" +
			"      o = null;\n" +
			"      if (o == null) { /* */ }\n" + // may have considered complaining here
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0320_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0321_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    Object other = new Object();\n" +
			"    if (b) {\n" +
			"      other = o;\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0322_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    o.toString();\n" +
			"    if (b) { /* */ }\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 5)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0323_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o == null && b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0324_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo (boolean b) {
				    String s = null;
				    if (b) {
				      if (b) {
				        s = "1";
				      }\s
				      else {
				        s = "2";
				      }
				    }\s
				    else if (b) {
				      s = "3";\s
				    }\s
				    else {
				      s = "4";
				    }
				    s.toString();
				  }
				}"""},
		"");
}

// null analysis - if/else
public void test0325_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo (boolean b) {
				    String s = null;
				    if (b) {
				      if (b) {
				        s = "1";
				      }\s
				      else {
				        s = "2";
				      }
				    }\s
				    else if (b) {
				      if (b) {
				        s = "3";\s
				      }
				    }\s
				    else {
				      s = "4";
				    }
				    s.toString();
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 20)
				s.toString();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// limit: we cannot sync on external factors, even if this is a pattern
// that is quite used
public void test0326_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo (boolean b) {\n" +
			"    String s1 = null;\n" +
			"    if (b) {\n" +
			"      s1 = \"1\";\n" +
			"    }\n" +
			"    s1.toString();\n" + // complain: can't guess if b means anything for s1 init
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				s1.toString();
				^^
			Potential null pointer access: The variable s1 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// limit: we cannot sync on external factors, even if this is a pattern
// that is quite used
public void test0327_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo (String s1) {\n" +
			"    String s2 = null;\n" +
			"    if (s1 == null) {\n" +
			"      s1 = \"1\";\n" +
			"      s2 = \"2\";\n" +
			"    }\n" +
			"    s1.toString();\n" + // quiet
			"    s2.toString();\n" + // complain: can't guess whether s2 depends on s1 for init
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				s2.toString();
				^^
			Potential null pointer access: The variable s2 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0328_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (o != null || b) {\n" +
			"      if (b) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" + // quiet
			"    else { /* */ }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0329_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      if (o != null) { /* */ }\n" + // shade doubts on o
			"    }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
public void test0330_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    if (b) {\n" +
			"      if (o == null) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - if/else
public void test0331_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o1, Object o2) {
				    Object o3 = o2;
				    if (o1 != null) {
				      o3.toString(); // guards o3
				    }
				    o1 = o3;
				    if (o1 != null) { /* */ }
				  }
				}"""},
		"");
}

// null analysis - if/else
public void test0332_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    o = new Object();\n" +
			"    if (b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128014
// invalid analysis when redundant check is done
public void test0333_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = new Object();\n" +
			"    if (o != null) {\n" + // complain
			"      o.toString();\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet asked
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128014
// invalid analysis when redundant check is done - variant
public void test0334_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = new Object();\n" +
			"    if (o != null) {\n" + // complain
			"      o.toString();\n" +
			"    }\n" +
			"    else {\n" +
			"      o.toString();\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 7)
				else {
			      o.toString();
			    }
				     ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129581
// Test that no false null reference warning is issued for a variable
// that has been wrongly tainted by a redundant null check upstream.
public void test0335_if_else() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" +
			"      if (o != null) {\n" + // complain
			"        o.toString();\n" +
			"      }\n" +
			"      o.toString();\n" + // quiet
			"    }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""");
}

// null analysis - if/else
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128014
// invalid analysis when redundant check is done - variant
public void test0336_if_else() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    if (o != null) {\n" +
			"      if (o != null) {\n" + // complain
			"        o.toString();\n" +
			"      }\n" +
			"      else {\n" +
			"        o.toString();\n" + // must complain anyway (could be quite distant from the if test)
			"      }\n" +
			"      o.toString();\n" + // quiet
			"    }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 7)
				else {
			        o.toString();
			      }
				     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}


// null analysis - if/else nested with correlation
// reconsider if we implement correlation
// TODO (maxime) https://bugs.eclipse.org/bugs/show_bug.cgi?id=128861
public void _test0337_if_else_nested_correlation() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public int foo (Object o1, Object o2) {
				    int result = 0;
				    if (o1 == null && o2 != null) {
				      result = -1;
				    } else {
				      if (o1 == null && o2 == null) {
				        result = 0;
				      } else {
				        if (o1 != null && o2 == null) {
				          result = 1;
				        } else {
				          int lhs = ((Y) o1).foo();  // may be null
				          int rhs = ((Y) o2).foo();
				          result = lhs - rhs;
				        }
				      }
				    }
				    return result;
				  }
				}
				abstract class Y {
				  abstract int foo();
				}
				
				"""},
		"");
}

// null analysis - if/else nested with correlation
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128861
// workaround
public void test0338_if_else_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public int foo (Object o1, Object o2) {
				    int result = 0;
				    if (o1 == null && o2 == null) {
				      result = 0;
				    } else {
				      if (o1 == null) {
				        result = -1;
				      } else {
				        if (o2 == null) {
				          result = 1;
				        } else {
				          int lhs = ((Y) o1).foo();
				          int rhs = ((Y) o2).foo();
				          result = lhs - rhs;
				        }
				      }
				    }
				    return result;
				  }
				}
				abstract class Y {
				  abstract int foo();
				}
				
				"""},
		"");
}

// null analysis - if/else nested with unknown protection: unknown cannot protect
public void test0339_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, boolean b) {
				    if (o == null || b) {
				      if (bar() == o) {
				        o.toString();
				      }
				    }
				  }
				  Object bar() {
				    return new Object();
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else nested
public void test0340_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    if (o == null) {
				      if (bar() == o) {
				        o.toString();
				      }
				    }
				  }
				  Object bar() {
				    return new Object();
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else nested
public void test0341_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o1, Object o2, boolean b) {
				    if (o1 == null || b) {
				      if (o1 == o2) {
				        o1.toString();
				      }
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o1.toString();
				^^
			Potential null pointer access: The variable o1 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - if/else nested
public void test0342_if_else_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o1, Object o2, boolean b) {
				    if (o1 == null || b) {
				      if (o2 == o1) {
				        o1.toString();
				      }
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o1.toString();
				^^
			Potential null pointer access: The variable o1 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0401_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o.toString() != null) {/* */}\n" +
			      // complain: NPE
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				while (o.toString() != null) {/* */}
				       ^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0402_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o != null) {/* */}\n" +
			  // complain: get o null first time and forever
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				while (o != null) {/* */}
				       ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0403_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			      // quiet: first iteration is sure to find o null,
			      // but other iterations may change it
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0404_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			     // quiet: first iteration is sure to find o null,
			     // but other iterations may change it
			"      if (System.currentTimeMillis() > 10L) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0405_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    while (bar() && o == null) {\n" +
			"      o.toString();\n" + // complain: NPE
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0406_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    while (dummy || o != null) { /* */ }\n" + // o can only be null
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				while (dummy || o != null) { /* */ }
				                ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0407_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      o.toString();\n" +  // complain: NPE on first iteration
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
// this test shows that, as long as we do not explore all possible
// paths, we have to take potential initializations into account
// even in branches that could be pruned in the first passes
// first approximation is to stop pruning code conditioned by
// variables
// second approximation could still rely upon variables that are
// never affected by the looping code (unassigned variables)
// complete solution would call for multiple iterations in the
// null analysis
public void test0408_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null,
				           u = new Object(),
				           v = new Object();
				    while (o == null) {
				      if (v == null) {
				        o = new Object();
				      };
				      if (u == null) {
				        v = null;
				      };
				      u = null;
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- while
public void test0409_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo() {
				    Object o = null;
				    while (dummy || (o = new Object()).equals(o)) {
				      o.toString();
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0410_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo() {
				    Object o = null;
				    while (dummy) {
				      while (o != null) {
				        o.toString();
				      }
				      if (System.currentTimeMillis() > 10L) {
				        o = new Object();
				      }
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- while
public void test0411_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null,
				           u = new Object(),
				           v = new Object();
				    while (o == null) {
				      if (v == null) {
				        o = new Object();
				      };
				      while (o == null) {
				        if (u == null) {
				          v = null;
				        };
				        u = null;
				      }
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- while
public void test0412_while_if_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy, other;
				  void foo() {
				    Object o = null;
				    while (dummy) {
				      if (other) {
				        o.toString();
				      }
				      o = new Object();
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0413_while_unknown_field() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object o;
				  void foo(boolean dummy) {
				    while (dummy) {
				      o = null;
				    }
				    o.toString();
				  }
				}
				"""},
		"");
}

// null analysis -- while
public void test0414_while_unknown_parameter() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    while (dummy) {\n" +
			"      o = null;\n" + // quiet: first iteration doesn't know
			"    }\n" +
			"    o.toString();\n" + // complain: only get out of the loop with null
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0415_while_unknown_if_else() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    if (dummy) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    while (dummy) {\n" +
			  // limit of the analysis: we do not correlate if and while conditions
			"      if (o == null) {/* */}\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0416_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo() {
				    Object o = null;
				    while (dummy) {
				      o = new Object();
				    }
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 8)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0417_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    while (dummy) { /* */ }\n" + // doesn't affect o
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
// origin AssignmentTest.testO22
public void test0418_while_try() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean bool() { return true; }
				  void foo() {
				    Object o = null;
				    while (bool()) {
				      try {
				        if (o == null) {
				          o = new Object();
				        }
				      } finally { /* */ }
				    }
				  }
				}"""},
		"");
}

// null analysis -- while
public void test0419_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo(Object o) {\n" +
			"    while (bool) {\n" +
			"      o.toString();" + // complain NPE because of second iteration
			"      o = null;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o.toString();      o = null;
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0420_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bool;\n" +
			"  void foo(Object compare) {\n" +
			"    Object o = new Object();\n" +
			"    while ((o = null) == compare) {\n" +
			"      if (true) {\n" +
			"        break;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" + // complain can only be null
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if (o == null) { /* */ }
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0421_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean bool;
				  void foo(Object compare) {
				    Object o = null;
				    while (bool) {
				      o = new Object();
				      o.toString();
				    }
				  }
				}"""},
		"");
}

// null analysis -- while
public void test0422_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean bool;
				  void foo() {
				    Object o;
				    while (bool) {
				      o = new Object();
				      if (o == null) { /* */ }
				      o = null;
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 7)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0423_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean bool;
				  void foo() {
				    Object o = null;
				    while (bool) {
				      o = new Object();
				      if (o == null) { /* */ }
				      o = null;
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 7)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0424_while_try() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			     // quiet: first iteration is sure to find o null,
			     // but other iterations may change it
			"      try { /* */ }\n" +
			"      finally {\n" +
			"        if (b) {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- while
public void test0425_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo(Object u) {
				    Object o = null;
				    while (dummy) {
				      o = u;
				    }
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 8)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0426_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo(Object o) {\n" +
			"    o.toString();\n" +
			"    while (dummy) { /* */ }\n" +
			"    if (o == null) { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 6)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- while
public void test0427_while_return() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo() {
				    Object o = null;
				    while (dummy) {
				      if (o == null) {
				        return;
				      }
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0428_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  X bar() {
				    return null;
				  }
				  void foo(X x) {
				    x.bar();
				    while (x != null) {
				      x = x.bar();
				    }
				  }
				}
				"""},
		"");
}

// null analysis - while
public void test0429_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo (X[] xa) {
					while (dummy) {
					  xa = null;
					  if (dummy) {
					    xa = new X[5];
					  }
					  if (xa != null) {
						int i = 0;
						while (dummy) {
						  X x = xa[i++];
						  x.toString();
						}
					  }
					}
				  }
				}"""},
		"");
}

// null analysis - while
public void test0430_while_for_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo (X[] xa) {
					while (dummy) {
					  xa = null;
					  if (dummy) {
					    xa = new X[5];
					  }
					  if (xa != null) {
						for (int i = 0; i < xa.length; i++) {
						  X x = xa[i];
						  x.toString();
						}
					  }
					}
				  }
				}"""},
		"");
}

// null analysis - while
public void test0431_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo (X x) {
					x = null;
					while (dummy) {
					  x = bar();
					  x.toString();
					}
				  }
				  X bar() {
					return null;
				  }
				}"""},
		"");
}

// null analysis - while
public void test0432_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  boolean dummy;
				  void foo (X x) {
					while (dummy) {
					  x = bar();
					  x.toString();
					}
				  }
				  X bar() {
					return null;
				  }
				}"""},
		"");
}

// null analysis - while
public void test0433_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean dummy;\n" +
			"  void foo (X x) {\n" +
			"	x = null;\n" +
			"   while (dummy) {\n" +
			"	  x.toString();\n" + // complain and protect
			"	  x.toString();\n" + // quiet
			"	}\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				x.toString();
				^
			Null pointer access: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// this one shows that we cannot project definitely unknown onto potentially unknown too soon
public void test0434_while_switch_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  Object bar() {
				    return new Object();
				  }
				  void foo(boolean b, int selector) {
				    Object o = null;
				    while (b) {
				      switch (selector) {
				      case 0:
				        o = bar();
				        if (o != null) {\s
				          return;
				        }
				      }
				    }
				  }
				}"""},
		"");
}

// null analysis - while
public void test0435_while_init() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  int f1;\n" +
			"  X f2;\n" +
			"  void foo(X x1, boolean b) {\n" +
			"    X x2;\n" +
			"    x2 = x1;\n" +
			"    while (b) {\n" +
//			"      if (x2.f1 > 0) { /* */ }\n" +
			"      if (x2.toString().equals(\"\")) { /* */ }\n" +
			"      x2 = x2.f2;\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0436_while_init() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  int f1;
				  X f2;
				  void foo(X x1, boolean b) {
				    X x2 = x1;
				    while (b) {
				      if (x2.f1 > 0) { /* */ }
				      x2 = x2.f2;
				    }
				  }
				}"""},
		"");
}

// null analysis - while
public void test0437_while_exit() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(boolean b) {
				    Object o = null;
				    while (b) {
				      if (b) {
				        o = new Object();
				      }
				      if (o != null) {
				        throw new RuntimeException();\s
				      }
				    }
				  }
				}"""},
		"");
}


// null analysis - while
public void test0438_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) { /* */ }\n" +
			"    o.toString();\n" + // quiet
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0439_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    o.toString();\n" + // quiet
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0440_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0441_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  X bar() {\n" +
			"    return new X();\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null) {\n" +
			"      o = bar();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0442_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    while (o == null && bar()) { /* */ }\n" +
			"    o.toString();\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0443_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo() {
				    Object o = null;
				    ext: for (int i = 0; i < 5 ; i++) {
				        if (o != null) {
				          break;
				        }
				        o = new Object();
				        int j = 0;
				        while (j++ < 2) {
				          continue ext;
				        }
				        return;
				    }
				  }
				}"""},
		"");
}

// null analysis - while
public void test0444_while_deeply_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(boolean b) {
				    Object o = null;
				    ext: for (int i = 0; i < 5 ; i++) {
				        if (o != null) {
				          break;
				        }
				        do {
				          o = new Object();
				          int j = 0;
				          while (j++ < 2) {
				            continue ext;
				          }
				        } while (b);
				        return;
				    }
				  }
				}"""},
		"");
}

// null analysis - while
public void test0445_while_deeply_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(boolean b) {
				    Object o = null;
				    ext: for (int i = 0; i < 5 ; i++) {
				        if (o != null) {
				          break;
				        }
				        do {
				          // o = new Object();
				          int j = 0;
				          while (j++ < 2) {
				            continue ext;
				          }
				        } while (b);
				        return;
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o != null) {
				    ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0446_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    while (o == null || b) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
public void test0447_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(Object o, boolean b) {
				    while (o == null & b) {
				      o = new Object();
				    }
				    if (o != null) { /* */ }
				  }
				}"""},
		"");
}

// null analysis - while
public void test0448_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(boolean b[]) {
				    Object o = null;
				    ext: for (int i = 0; i < 5 ; i++) {
				        if (o != null) {
				          break;
				        }
				        while (b[1]) {
				          continue ext;
				        }
				        while (b[2]) {
				          continue ext;
				        }
				        while (b[3]) {
				          continue ext;
				        }
				        while (b[4]) {
				          continue ext;
				        }
				        while (b[5]) {
				          continue ext;
				        }
				        while (b[6]) {
				          continue ext;
				        }
				        return;
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o != null) {
				    ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// this series (up to 451) shows that the merge of the states
// potential non null and potential unknown yields damages in
// case of nested loops (unested loops still OK because we can
// carry the definite non null property)
public void test0449_while_nested() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object p, boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      while (b) {\n" +
			"        o = p;\n" + // now o is unknown
			"      }\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
public void test0450_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      o = new Object();\n" + // o still non null
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133131
public void test0451_while_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      while (b) {\n" +
			"        o = new Object();\n" + // o still non null
			"      }\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant - the bug is not specific to the do while loop
public void _test0452_while() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object doubt) {
				    Object o = null;
				    while (true) {
				      if (o == null) {
				        return;
				      }
				      o = doubt;
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant - cannot refine the diagnostic without engaging into conditionals
// dedicated flow context
public void _test0453_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object doubt, boolean b) {
				    Object o1 = null, o2 = null;
				    while (true) {
				      if (o1 == null) { /* empty */ }
				      if (b) {
				        if (o2 == null) {
				          return;
				        }
				      }
				      o1 = o2 = doubt;
				    }
				  }
				}"""},
		"ERROR: complain on line 7, but not on line 5"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129122
public void test0454_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object bar() {
				    return new Object();
				  }
				  void foo() {
				    Object o = null;
				    while (true) {
				      o = bar();
				      if (o != null) {
				        o = new Object();
				      }
				      o = null; // quiet pls
				    }
				  }
				}"""},
		""
	);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133131
// variant
public void test0455_while_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = new Object();\n" +
			"    while (b) {\n" +
			"      o = new Object();\n" + // o still non null
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=134848
// false positive after nested loop with break to explicit label
public void test0456_while_nested_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (true) {\n" +
			"      if (o != null) {\n" +
			"        o.toString();\n" +
			"        loop: while (true) {\n" +
			"          break loop;\n" +
			"        }\n" +
			"        o.toString();\n" + // must not complain here
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=154995
public void test0457_while_nested_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void test(String p, String q, boolean b) {\n" +
			"    while (b) {\n" +
			"      String e = q;\n" +
			"      e.trim();\n" +
			"      while (true) {\n" +
			"        if (b)\n" +
			"          e = q;\n" +
			"        else\n" +
			"          e = null;\n" +
			"        if (e == null || p != null) {\n" +
			"          if (e != null) {\n" + // should not complain here
			"            // Do something\n" +
			"          }\n" +
			"          break;\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=134848
// variant: no label yields no problem
public void test0458_while_nested_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    while (true) {\n" +
			"      if (o != null) {\n" +
			"        o.toString();\n" +
			"        while (true) {\n" +
			"          break;\n" +
			"        }\n" +
			"        o.toString();\n" + // must not complain here
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- while nested hits CAN_ONLY_NON_NULL
public void test0459_while_nested() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(boolean b) {
				    Object o = b ? null : new Object(),
				           u = new Object(),
				           v = new Object();
				    while (o != null) {
				      while (b) {
				        if (v == null) {
				          o = new Object();
				        };
				        while (o == null) {
				          if (u == null) {
				            v = null;
				          };
				          u = null;
				        }
				      }
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 11)
				while (o == null) {
				       ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// extraneous error in case of a labeled while(true) statement
public void test0460_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(int i) {
				    Object o = null;
				    done: while (true) {
				      switch (i) {
				        case 0:
				          o = new Object();
				          break;
				        case 1:
				          break done;
				      }
				    }
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// extraneous error in case of a labeled while(true) statement
public void test0461_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean test() {
				    return true;
				  }
				  void foo() {
				    Object o = null;
				    done: while (true) {
				      if (test()) {
				        break done;
				      }
				      o = new Object();
				    }
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0462_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean test() {
				    return true;
				  }
				  void foo() {
				    Object o = null;
				    done: while (true) {
				      try {
				        while (true) {
				          if (test()) {
				            break done;
				          }
				        }
				      }
				      finally {
				        if (test()) {
				          o = new Object();
				        }
				      }
				    }
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0463_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    while(true)
				    {
				      if (a[0] == null)
				        break;
				      key = a[0];
				    }
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0464_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    loop: while(true)
				    {
				      if (a[0] == null)
				        break loop;
				      key = a[0];
				    }
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0465_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    while(true)
				    {
				      if (a[0] == null)
				        break;
				      key = "non null";
				    }
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis - while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=198955
// dupe of bug 184298 in fact
public void test0466_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    while (true) {
				      if (bar()) {
				        break;
				      }
				      if (o == null) {
				        o = new Object();
				      }
				    }
				    if (o == null) {}
				  }
				  boolean bar() {
				    return false;
				  }
				}
				"""},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212283
// (which is a dupe of 184298)
public void test0467_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    RuntimeException e = null;\n" +
			"    while (e != null || bar()) {\n" +
			"      if (e != null || bar()) {\n" +
			"        break;\n" +  // always breaks out of the loop if e non-null
			"      }\n" +
			"      if (bar()) {\n" +
			"        e = new RuntimeException();\n" +
			"      }\n" +
			"    }\n" +
			"    if (e != null) {\n" +
			"      throw e;\n" +
			"    }\n" +
			"  }\n" +
			"  boolean bar() {\n" +
			"    return false;\n" +
			"  }\n" +
			"}"
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212283
// (which is a dupe of 184298)
public void test0468_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    RuntimeException e = null;
				    while (e != null || bar()) {
				      if (bar()) {
				        break;
				      }
				      if (bar()) {
				        e = new RuntimeException();
				      }
				    }
				    if (e != null) {
				      throw e;
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212283
// (which is a dupe of 184298)
public void test0469_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    RuntimeException e = null;
				    while (e != null || bar()) {
				      if (e != null) {
				        break;
				      }
				      if (bar()) {
				        e = new RuntimeException();
				      }
				    }
				    if (e != null) {
				      throw e;
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=220788
public void test0470_while() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = new Object();
				    while (bar()) {
				      if (o != null && o.toString().equals("o")) {
				      }
				    }
				    if (o.toString().equals("o")) {
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o != null && o.toString().equals("o")) {
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""");
}
// null analysis -- try/finally
public void test0500_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m;
				  void foo() {
				    Object o = null;
				    try { /* */ }
				    finally {
				      o = m;
				    }
				    o.toString();
				  }
				}
				"""},
		"" // because finally assigns to unknown value
	);
}

// null analysis -- try/finally
public void test0501_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = new Object();
				    try { /* */ }
				    finally {
				      o = null;
				    }
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 8)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""", // because finally assigns to null
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
public void test0502_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"      System.out.println();\n" + // might throw a runtime exception
			"      o = new Object();\n" +
			"    }\n" +
			"    finally { /* */ }\n" +
			"    o.toString();\n" +
			    // still OK because in case of exception this code is
			    // not reached
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
public void test0503_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    try {\n" +
			"      x = null;\n" +                // complain, already null
			"    } finally { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				x = null;
				^
			Redundant assignment: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
public void test0504_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    try {\n" +
			"    } finally {\n" +
			"      if (x != null) { /* */ }\n" + // complain null
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (x != null) { /* */ }
				    ^
			Null comparison always yields false: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// origin: AssignmentTest#test017
// The whole issue here is whether or not to detect premature exits.
// Previously, we followed JLS's conservative approach, which considers
// that the try block may exit before the assignment is completed.
// As of Bug 345305 this has been changed to a more accurate analysis.
public void test0505_try_finally() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo(X x) {\n" +
			"   x = this;\n" + // 1
			"   try {\n" +
			"     x = null;\n" +
			"   } finally {\n" +
			"     if (x == null) {/* */}\n" + // 2
			"   }\n" +
			" }\n" +
			"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 7)
					if (x == null) {/* */}
					    ^
				Redundant null check: The variable x can only be null at this location
				----------
				""");
}

// null analysis -- try finally
public void test0506_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    try { /* */ }
				    finally {
				      o = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 7)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0507_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o, boolean b) {\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o.toString();\n" +  // protect
			"    }\n" +
			"    if (o == null) {\n" + // complain
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (o == null) {
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 7)
				if (o == null) {
			      o = new Object();
			    }
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0508_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = null;" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o.toString();\n" +  // complain and protect
			"      o.toString();\n" +  // quiet
			"    }\n" +
			"    o.toString();\n" +  // quiet
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0509_try_finally_embedded() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1) {\n" +
			"    Object o2 = null;" +
			"    while (true) {\n" +
			"      // o2 = o1;\n" +
			"      try { /* */ }\n" +
			"      finally {\n" +
			"        o2.toString();\n" +  // complain and protect
			"        o2.toString();\n" +  // quiet
			"      }\n" +
			"      o2.toString();\n" +  // quiet
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o2.toString();
				^^
			Null pointer access: The variable o2 can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
public void test0510_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void bar() throws Exception {
				    // empty
				  }
				  void foo(Object o, boolean b) throws Exception {
				    try {
				      bar();
				      if (b) {
				        o.toString();
				      }
				    }
				    finally {
				      if (o != null) {
				          o.toString();
				      }
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- try finally
public void test0511_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o1, boolean b) {\n" +
			"    Object o2 = null;\n" +
			"    if (b) {\n" +
			"      o2 = new Object();\n" +
			"    }\n" + 				// 0011
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o2 = o1;\n" + 		// 1011
			"    }\n" +
			"    o2.toString();\n" + 	// 1011 -- quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
public void test0512_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 void foo(X x) {
				   x = null;
				   try {
				     x = new X();
				   } finally {
				     x.toString();
				   }
				 }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				x.toString();
				^
			Potential null pointer access: The variable x may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
public void test0513_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 X bar() {
				   return null;
				 }
				 Object foo() {
				   X x = null;
				   try {
				     x = bar();
				     x.toString();
				     return x;
				   } finally {
				     if (x != null) {
				       x.toString();
				     }
				   }
				 }
				}
				"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
// embedded variant 1
public void test0514_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 X bar() {
				   return null;
				 }
				 Object foo() {
				   X x = null;
				   try {
				     try {
				       x = bar();
				       x.toString();
				       return x;
				     }
				     finally {
				     }
				   }
				   finally {
				     if (x != null) {
				       x.toString();
				     }
				   }
				 }
				}
				"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
// embedded variant 2
public void test0515_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 X bar() {
				   return null;
				 }
				 Object foo() {
				   X x = null;
				   try {
				     try {
				       x = bar();
				       x.toString();
				       return x;
				     }
				     finally {
				       System.out.println();
				     }
				   }
				   finally {
				     if (x != null) {
				       x.toString();
				     }
				   }
				 }
				}
				"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128547
// variant
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184546
// variant
public void test0516_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 Object foo() {
				   X x = null;
				   try {
				     x = new X();
				     return x;
				   }
				   finally {
				     if (x != null) {
				       x.toString();
				     }
				   }
				 }
				}
				"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132072
// AIOOBE in null check compiling com.sun.org.apache.xalan.internal.res.XSLTErrorResources from JDK 1.5 source
public void test0517_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 Object foo() {
				   String s00, s01, s02, s03, s04, s05, s06, s07, s08, s09;
				   String s10, s11, s12, s13, s14, s15, s16, s17, s18, s19;
				   String s20, s21, s22, s23, s24, s25, s26, s27, s28, s29;
				   String s30, s31, s32, s33, s34, s35, s36, s37, s38, s39;
				   String s40, s41, s42, s43, s44, s45, s46, s47, s48, s49;
				   String s50, s51, s52, s53, s54, s55, s56, s57, s58, s59;
				   String s60, s61, s62, s63, s64, s65, s66, s67, s68, s69;
				   String s100, s101, s102, s103, s104, s105, s106, s107, s108, s109;
				   String s110, s111, s112, s113, s114, s115, s116, s117, s118, s119;
				   String s120, s121, s122, s123, s124, s125, s126, s127, s128, s129;
				   String s130, s131, s132, s133, s134, s135, s136, s137, s138, s139;
				   String s140, s141, s142, s143, s144, s145, s146, s147, s148, s149;
				   String s150, s151, s152, s153, s154, s155, s156, s157, s158, s159;
				   String s160, s161, s162, s163, s164, s165, s166, s167, s168, s169;
				   String s200, s201, s202, s203, s204, s205, s206, s207, s208, s209;
				   String s210, s211, s212, s213, s214, s215, s216, s217, s218, s219;
				   String s220, s221, s222, s223, s224, s225, s226, s227, s228, s229;
				   String s230, s231, s232, s233, s234, s235, s236, s237, s238, s239;
				   String s240, s241, s242, s243, s244, s245, s246, s247, s248, s249;
				   String s250, s251, s252, s253, s254, s255, s256, s257, s258, s259;
				   String s260, s261, s262, s263, s264, s265, s266, s267, s268, s269;
				   X x = new X();
				   try {
				     return x;
				   }
				   finally {
				   }
				 }
				}
				"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=132120
// [compiler][null] NPE batch compiling JDT/Core from HEAD
public void test0518_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 void foo() {
				   String s00, s01, s02, s03, s04, s05, s06, s07, s08, s09;
				   String s10, s11, s12, s13, s14, s15, s16, s17, s18, s19;
				   String s20, s21, s22, s23, s24, s25, s26, s27, s28, s29;
				   String s30, s31, s32, s33, s34, s35, s36, s37, s38, s39;
				   String s40, s41, s42, s43, s44, s45, s46, s47, s48, s49;
				   String s50, s51, s52, s53, s54, s55, s56, s57, s58, s59;
				   String s60, s61, s62, s63, s64, s65, s66, s67, s68, s69;
				   String s100, s101, s102, s103, s104, s105, s106, s107, s108, s109;
				   String s110, s111, s112, s113, s114, s115, s116, s117, s118, s119;
				   String s120, s121, s122, s123, s124, s125, s126, s127, s128, s129;
				   String s130, s131, s132, s133, s134, s135, s136, s137, s138, s139;
				   String s140, s141, s142, s143, s144, s145, s146, s147, s148, s149;
				   String s150, s151, s152, s153, s154, s155, s156, s157, s158, s159;
				   String s160, s161, s162, s163, s164, s165, s166, s167, s168, s169;
				   String s200, s201, s202, s203, s204, s205, s206, s207, s208, s209;
				   String s210, s211, s212, s213, s214, s215, s216, s217, s218, s219;
				   String s220, s221, s222, s223, s224, s225, s226, s227, s228, s229;
				   String s230, s231, s232, s233, s234, s235, s236, s237, s238, s239;
				   String s240, s241, s242, s243, s244, s245, s246, s247, s248, s249;
				   String s250, s251, s252, s253, s254, s255, s256, s257, s258, s259;
				   String s260, s261, s262, s263, s264, s265, s266, s267, s268, s269;
				   X x = null;
				   try {
				     x = new X();
				   } finally {
				     x.toString();
				   }
				 }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 28)
				x.toString();
				^
			Potential null pointer access: The variable x may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128962
// incorrect analysis within try finally with a constructor throwing an exception
public void test0519_try_finally_constructor_exc() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(Y y) throws E {\n" +
			"    try {\n" +
			"      new Y();\n" +
			"      y.toString();\n" + // should be quiet
			"    } finally {\n" +
			"      y = null;\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"class Y {\n" +
			"  Y() throws E {\n" +
			"  }\n" +
			"}\n" +
			"class E extends Exception {\n" +
			"  private static final long serialVersionUID = 1L;\n" +
			"}\n"},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128962
// incorrect analysis within try finally with a constructor throwing an exception
// variant
public void test0520_try_finally_constructor_exc() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo(Y y) throws E {\s
				    try {\s
				      new Y() {
				          void bar() {
				              // do nothing
				          }
				      };\s
				      y.toString();
				    } finally {\s
				      y = null;\s
				    }\s
				  }\s
				}
				abstract class Y {
				  Y() throws E {\s
				  }
				  abstract void bar();
				}\s
				class E extends Exception {
				  private static final long serialVersionUID = 1L;
				}"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// incorrect analysis within try finally with an embedded && expression
public void test0521_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  X m;
				  public void foo() {
				    for(int j = 0; j < 10; j++) {
				      try {
				        j++;
				      } finally {
				        X t = m;
				        if( t != null && t.bar()) {
				        }
				      }
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// variant
public void test0522_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  X m;
				  public void foo() {
				    for(int j = 0; j < 10; j++) {
				      try {
				        j++;
				      } finally {
				        X t = null;
				        if(t.bar()) {
				        }
				      }
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if(t.bar()) {
				   ^
			Null pointer access: The variable t can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// variant
public void test0523_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  X m;
				  public void foo() {
				    for(int j = 0; j < 10; j++) {
				      try {
				        j++;
				      } finally {
				        X t = m;
				        if(t == null ? false : (t == null ? false : t.bar())) {
				        }
				      }
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if(t == null ? false : (t == null ? false : t.bar())) {
				                        ^
			Null comparison always yields false: The variable t cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149665
// variant
public void test0524_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  X m;
				  public void foo() {
				    for(int j = 0; j < 10; j++) {
				      try {
				        j++;
				      } finally {
				        X t = m;
				        if(t != null ? false : (t == null ? false : t.bar())) {
				        }
				      }
				    }
				  }
				  boolean bar() {
				    return false;
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if(t != null ? false : (t == null ? false : t.bar())) {
				                        ^
			Redundant null check: The variable t can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150082
public void _test0525_try_finally_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  String foo(Object p) {
				    String s = null;
				    Object o = null;
				    try {
				        o = p;
				        if (o == null) {
				          return null;
				        }
				        s = o.getClass().getName();
				    } catch (RuntimeException e) {
				            o.toString();
				            s = null;
				    } finally {
				      if (o != null) {
				      }
				    }
				    return s;
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 13)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150082
// variant
public void test0526_try_finally_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  String foo(Object p) {\n" +
			"    String s = null;\n" +
			"    Object o = p;\n" +
			"    try {\n" +
			"        if (o == null) {\n" +  // shades doubts upon o
			"          return null;\n" +	// may throw a RuntimeException by spec
			"        }\n" +
			"        s = o.getClass().getName();\n" +
			"    } catch (RuntimeException e) {\n" +
			"            o.toString();\n" +
			"            s = null;\n" +
			"    } finally {\n" +
			"      if (o != null) {\n" +
			"      }\n" +
			"    }\n" +
			"    return s;\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//null analysis -- try/finally
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150082
//variant
public void test0527_try_finally_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" +
			"{\n" +
			"  String foo(Object p) {\n" +
			"    String s = null;\n" +
			"    Object o = p;\n" +
			"    try {\n" +
			"        if (o == null) {\n" +  // shades doubts upon o
			"          return null;\n" +	// may throw a RuntimeException by spec
			"        }\n" +
			"        s = o.getClass().getName();\n" +
			"    } catch (RuntimeException e) {\n" +
			"            o.toString();\n" +
			"            s = null;\n" +
			"    }\n" +
			"    return s;\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0528_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    x = null;\n" +
			"    X y = null;\n" +
			"    try {\n" +
			"    } finally {\n" +
			"      if (x != null) { /* */ }\n" + // complain null
			"      if (y != null) { /* */ }\n" + // complain null as well
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (x != null) { /* */ }
				    ^
			Null comparison always yields false: The variable x can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				if (y != null) { /* */ }
				    ^
			Null comparison always yields false: The variable y can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0529_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    o = null;\n" +
			"    Object o2 = null;\n" +
			"    try { /* */ }\n" +
			"    finally {\n" +
			"      o.toString();\n" +  // complain
			"      o2.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				o2.toString();
				^^
			Null pointer access: The variable o2 can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0530_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo(X x) {\n" +
			"   x = null;\n" +
			"   X y = null;\n" +
			"   try {\n" +
			"     x = new X();\n" +
			"   } finally {\n" +
			"     x.toString();\n" +
			"     y.toString();\n" + // complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 8)
				x.toString();
				^
			Potential null pointer access: The variable x may be null at this location
			----------
			2. ERROR in X.java (at line 9)
				y.toString();
				^
			Null pointer access: The variable y can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158000
public void test0531_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" void foo() {\n" +
			"   X x = new X();\n" +
			"   X y = null;\n" +
			"   try {\n" +
			"   } finally {\n" +
			"     if (x != null) {\n" +
			"       x.toString();\n" +
			"     }\n" +
			"     y.toString();\n" + // complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (x != null) {
				    ^
			Redundant null check: The variable x cannot be null at this location
			----------
			2. ERROR in X.java (at line 10)
				y.toString();
				^
			Null pointer access: The variable y can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=177863
public void test0532_try_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void foo() {
				    Object o = null;
				    try {
				    } finally {
				      o = Object.class.getClass();
				      o.getClass();
				    }
				  }
				}"""},
		"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184546
public void test0533_try_finally_field() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 static char SHOULD_NOT_MATTER = '?';
				 Object foo() {
				   X x = null;
				   try {
				     x = new X();
				     return x;
				   }
				   finally {
				     if (x != null) {
				       x.toString();
				     }
				   }
				 }
				}
				"""},
		"");
}

// null analysis - try finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=198970
public void _test0534_try_finally() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    String foo = null;
				    boolean a = true;
				    try {
				    }
				    catch(Exception e) {
				    }
				    finally {
				      if (a) {
				        foo = new String();
				      }
				      if (foo != null) {
				      }
				    }
				  }
				}
				"""
			},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=295260
public void test0535_try_finally() {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void test3(String[] args) {\n" +
				"		while (true) {\n" +
				"			Object a = null;\n" +
				"			try {\n" +
				"				a = new Object();\n" +
				"			} catch (Exception e) {\n" +
				"			} finally {\n" +
				"				if (a != null)\n" +
				"					a = null;\n" +	// quiet
				"			}\n" +
				"		}\n" +
				"	}\n"+
				"}",
			},
			"");
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320170 -  [compiler] [null] Whitebox issues in null analysis
// trigger nullbits 0111 (pot n|nn|un), don't let "definitely unknown" override previous information
public void test0536_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar () { return null; }\n" +
			" void foo() {\n" +
			"   X x = new X();\n" +
			"   try {\n" +
			"     x = null;\n" +
			"     x = new X();\n" +  // if this throws an exception finally finds x==null
			"     x = bar();\n" +
			"   } finally {\n" +
			"     x.toString();\n" + // complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 10)
				x.toString();
				^
			Potential null pointer access: The variable x may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/finally
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320170 -  [compiler] [null] Whitebox issues in null analysis
// trigger nullbits 0111 (pot n|nn|un), don't let "definitely unknown" override previous information
// multiple variables
public void test0537_try_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar () { return null; }\n" +
			" void foo() {\n" +
			"   X x1 = new X();\n" +
			"   X x2 = new X();\n" +
			"   X x3 = new X();\n" +
			"   try {\n" +
			"     x1 = null;\n" +
			"     x2 = null;\n" +
			"     x1 = new X();\n" +  // if this throws an exception finally finds x1==null
			"     x2 = new X();\n" +  // if this throws an exception finally finds x2==null
			"     x3 = new X();\n" +  // if this throws an exception finally still finds x3!=null
			"     x1 = bar();\n" +
			"     x2 = bar();\n" +
			"   } finally {\n" +
			"     x1.toString();\n" + // complain
			"     x2.toString();\n" + // complain
			"     x3.toString();\n" + // don't complain
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 16)
				x1.toString();
				^^
			Potential null pointer access: The variable x1 may be null at this location
			----------
			2. ERROR in X.java (at line 17)
				x2.toString();
				^^
			Potential null pointer access: The variable x2 may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- try/catch
public void test0550_try_catch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"      System.out.println();\n" +  // might throw a runtime exception
			"      o = new Object();\n" +
			"    }\n" +
			"    catch (Throwable t) {\n" + // catches everything
			"      return;\n" +             // gets out
			"    }\n" +
			"    o.toString();\n" +         // non null
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - try/catch
public void test0551_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      System.out.println();\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new Exception();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      o.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 13)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0552_try_catch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() throws Exception {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new Exception();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"    }\n" +
			"    if (o != null) {\n" +
			  // quiet: get out of try either through normal flow, leaves a new
			  // object, or through Exception, leaves a null
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - try/catch
public void test0553_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy, other;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        if (other) {\n" +
			"          throw new LocalException();\n" + // may launch new exception
			"        }\n" +
			"        o = null;\n" +
			"        throw new LocalException();\n" + // must launch new exception
			"      }\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" +
			"      o.toString();\n" + // complain
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 15)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0554_try_catch() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) throws Exception {\n" +
			"    try {\n" +
			"      o = null;\n" +
			"      throwLocalException();\n" +
			"      throw new Exception();\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" +
			"    }\n" +
			"    if (o != null) {\n" +
			  // complain: only way to get out of try and get there is to go
			  // through throwLocalException, after the assignment
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  void throwLocalException() throws LocalException {\n" +
			"    throw new LocalException();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if (o != null) {
				    ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. WARNING in X.java (at line 10)
				if (o != null) {
			    }
				               ^^^^^^^
			Dead code
			----------
			"""
	);
}

// null analysis - try/catch
public void test0555_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      o = null;\n" +
			"      throwException();\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  void throwException() throws Exception {\n" +
			"    throw new Exception();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0556_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      o = null;\n" +
			"      throwException();\n" +
			"    }\n" +
			"    catch (Throwable t) {\n" +
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  void throwException() throws Exception {\n" +
			"    throw new Exception();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0557_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new Exception();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0558_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        System.out.print(0);\n" + // may thow RuntimeException
			"        o = null;\n" +
			"        throw new LocalException();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" + // doesn't catch RuntimeException
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 13)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0559_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = new Object();\n" +
			"    try {\n" +
			"      if (dummy) {\n" +
			"        o = null;\n" +
			"        throw new SubException();\n" +
			"      }\n" +
			"    }\n" +
			"    catch (LocalException e) {\n" + // must catch SubException
			"      o.toString();\n" + // complain NPE
			"    }\n" +
			"  }\n" +
			"  class LocalException extends Exception {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"  class SubException extends LocalException {\n" +
			"    private static final long serialVersionUID = 1L;\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
public void test0560_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Class bar(boolean b) throws ClassNotFoundException {
				    if (b) {
				      throw new ClassNotFoundException();
				    }
				    return null;
				  }
				  public Class foo(Class c, boolean b) {
				    if (c != null)
				      return c;
				    if (b) {
				      try {
				        c = bar(b);
				        return c;
				      } catch (ClassNotFoundException e) {
				      // empty
				      }
				    }
				    if (c == null) { // should complain: c can only be null
				    }
				    return c;
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 19)
				if (c == null) { // should complain: c can only be null
				    ^
			Redundant null check: The variable c can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=130359
public void test0561_try_catch_unchecked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"      o = bar();\n" +
			"    } catch (RuntimeException e) {\n" +
			"      o.toString();\n" + // may be null
			"    }\n" +
			"  }\n" +
			"  private Object bar() {\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// (slightly different) variant of 561
public void test0562_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"    LineNumberReader o = null;\n" +
			"    try {\n" +
			"      o = new LineNumberReader(new FileReader(\"dummy\"));\n" +
			"    } catch (NumberFormatException e) {\n" +
			"      o.toString();\n" + // may be null
			"    } catch (IOException e) {\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
			null,
			options,
			"""
				----------
				1. WARNING in X.java (at line 6)
					o = new LineNumberReader(new FileReader("dummy"));
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Potential resource leak: 'o' may not be closed
				----------
				2. ERROR in X.java (at line 8)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=155117
public void test0563_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo(boolean b) {\n" +
			"    Exception ex = null;\n" +
			"    if (b) {\n" +
			"      try {\n" +
			"        System.out.println();\n" +
			"        return;\n" +
			"      } catch (Exception e) {\n" +
			"        ex = e;\n" +
			"      }\n" +
			"    }\n" +
			"    else {\n" +
			"      try {\n" +
			"        System.out.println();\n" +
			"        return;\n" +
			"      } catch (Exception e) {\n" +
			"        ex = e;\n" +
			"      }\n" +
			"    }\n" +
			"    if (ex == null) {\n" + // complain: ex cannot be null\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 20)
				if (ex == null) {
				    ^^
			Null comparison always yields false: The variable ex cannot be null at this location
			----------
			2. WARNING in X.java (at line 20)
				if (ex == null) {
			    }
				                ^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
public void test0564_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static Object foo() {
				    Object result = null;
				    try {
				      result = new Object();
				    } catch (Exception e) {
				      result = null;
				    }
				    return result;
				  }
				}"""},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		options,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// variant
public void test0565_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static Object foo() {
				    Object result = null;
				    try {
				      result = new Object();
				      result = new Object();
				    } catch (Exception e) {
				      result = null;
				    }
				    return result;
				  }
				}"""},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		options,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}

// null analysis - try/catch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=150854
// variant
public void test0566_try_catch_unchecked_exception() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static Object foo(Y y) {
				    Object result = null;
				    try {
				      while (y.next()) {
				        result = y.getObject();
				      }
				    } catch (Exception e) {
				      result = null;
				    }
				    return result;
				  }
				}
				class Y {
				  boolean next() {
				    return false;
				  }
				  Object getObject() {
				    return null;
				  }
				}"""},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		options,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}

// null analysis - try/catch for checked exceptions
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=295260
public void test0567_try_catch_checked_exception() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.net.MalformedURLException;\n" +
				"import java.net.URL;\n" +
				"public class X {\n" +
				"	public void test1(String[] args) {\n" +
				"		URL[] urls = null;\n" +
				"		try	{\n" +
				"			urls = new URL[args.length];\n" +
				"			for (int i = 0; i < args.length; i++)\n" +
				"				urls[i] = new URL(\"http\", \"\", -1, args[i]);\n" +
				"		}\n" +
				"		catch (MalformedURLException mfex) {\n" +
				"			urls = null;\n" +	// quiet
				"		}\n" +
				"	}\n" +
				"}",
			},
			"");
}

// null analysis - try/catch for checked exceptions with finally block
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=295260
public void test0568_try_catch_checked_exception() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.net.MalformedURLException;\n" +
				"import java.net.URL;\n" +
				"public class X {\n" +
				"	public void test1(String[] args) {\n" +
				"		URL[] urls = null;\n" +
				"		try	{\n" +
				"			urls = new URL[args.length];\n" +
				"			for (int i = 0; i < args.length; i++)\n" +
				"				urls[i] = new URL(\"http\", \"\", -1, args[i]);\n" +
				"		}\n" +
				"		catch (MalformedURLException mfex) {\n" +
				"			urls = null;\n" +	// quiet
				"		}\n" +
				" 		finally{\n"+
				"			System.out.println(\"complete\");\n" +
				"		}\n" +
				"	}\n" +
				"}",
			},
			"");
}
// null analysis -- try/catch
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0569_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"	 int i;\n" +
			"	 if (o == null)\n" +	// redundant check
			"	 	i = 0;\n" +
			"    try {\n" +
			"      System.out.println(i);\n" +  // might throw a runtime exception
			"      o = new Object();\n" +
			"	   throw new Exception(\"Exception thrown from try block\");\n" +
			"    }\n" +
			"    catch (Throwable t) {\n" + // catches everything
			"      return;\n" +             // gets out
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null)
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				System.out.println(i);
				                   ^
			The local variable i may not have been initialized
			----------
			""");
}
// null analysis -- try/catch
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0570_try_catch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"	 int i;\n" +
			"	 if (o == null)\n" +	// redundant check
			"	 	i = 0;\n" +
			"    try {\n" +
			"      System.out.println();\n" +  // might throw a runtime exception
			"      o = new Object();\n" +
			"	   if (o != null)\n" +		// redundant check
			"			i = 1\n;" +
			"		throw new Exception(\"Exception thrown from try block\");\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      if(i == 0)\n" +
			"			System.out.println(\"o was initialised\");\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null)
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 10)
				if (o != null)
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			3. ERROR in X.java (at line 15)
				if(i == 0)
				   ^
			The local variable i may not have been initialized
			----------
			""");
}
//null analysis -- try/catch
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0571_try_catch_finally() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"	 int i;\n" +
			"	 if (o == null)\n" +	// redundant check
			"	 	i = 0;\n" +
			"    try {\n" +
			"      o = new Object();\n" +
			"	   i = 1\n;" +
			"	   throw new Exception(\"Exception thrown from try block\");\n" +
			"    }\n" +
			"    catch (Exception e) {\n" +
			"      if(o == null)\n" +
			"			o = new Object();\n" +
			"	   i = 1;\n" +
			"    }\n" +
			"	 finally {\n" +
			"		if (i==1) {\n" +
			"	 		System.out.println(\"Method ended with o being initialised\");\n" +
			"		System.out.println(o.toString());\n" +	// may be null
			"		} \n" +
			"	 }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null)
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 18)
				if (i==1) {
				    ^
			The local variable i may not have been initialized
			----------
			3. ERROR in X.java (at line 20)
				System.out.println(o.toString());
				                   ^
			Potential null pointer access: The variable o may be null at this location
			----------
			""");
}
//null analysis -- if statement
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=302446
public void test0572_if_statement() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		Object o = null;\n" +
			"		int i;\n" +
			"		if (o == null) // redundant check\n" +
			"			i = 0;\n" +
			"		System.out.println(i);\n" +
			"	}\n" +
			"}\n" +
			""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null) // redundant check
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 7)
				System.out.println(i);
				                   ^
			The local variable i may not have been initialized
			----------
			""");
}

// take care for Java7 changes
public void test0573_try_catch_unchecked_and_checked_exception() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    try {\n" +
			"		bar();\n" +
			"		o = new Object();\n" +
			"    } catch (IOException e) {\n" +
			"		o.toString();\n" +
			"    } catch(RuntimeException e) {\n" +
			"       o.toString();\n" + // may be null
			"    }\n" +
			"  }\n" +
			"  private Object bar() throws IOException{\n" +
			"    return new Object();\n" +
			"  }\n" +
			"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 9)
					o.toString();
					^
				Null pointer access: The variable o can only be null at this location
				----------
				2. ERROR in X.java (at line 11)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// similar to test0573 using multi catch parameters
public void test0574_try_multi_catch_unchecked_and_checked_exception() {
	if (this.complianceLevel >=  ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.IOException;
					public class X {
					  void foo() {
					    Object o = null;
					    try {
							bar();
							o = new Object();
					    } catch (IOException | RuntimeException e) {
							o.toString();
					    }
					  }
					  private Object bar() throws IOException{
					    return new Object();
					  }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 9)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}
//multi catch variant of test0561_try_catch_unchecked_exception
public void test0575_try_multi_catch_finally_unchecked_and_checked_exception() {
	if (this.complianceLevel >=  ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    try {\n" +
				"      o = bar();\n" +
				"    } catch (IOException | RuntimeException e) {\n" +
				"      o.toString();\n" + // may be null
				"    } finally {}\n" +
				"  }\n" +
				"  private Object bar() throws IOException{\n" +
				"    return new Object();\n" +
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 8)
					o.toString();
					^
				Potential null pointer access: The variable o may be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null test for resources inside try with resources statement
public void test0576_try_with_resources() {
	if (this.complianceLevel >=  ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.FileInputStream;\n" +
				"import java.io.IOException;\n" +
				"import java.io.FileNotFoundException;\n" +
				"class MyException extends Exception {}\n" +
				"public class X {\n" +
				"   static void m(int n) throws IllegalArgumentException, MyException {}\n" +
				"   void foo(String name, boolean b) throws FileNotFoundException, IOException{\n" +
				"    FileInputStream fis;\n" +
				"	 if (b) fis = new FileInputStream(\"\");\n" +
				"	 else fis = null;\n" +
				"    try (FileInputStream fis2 = fis; FileInputStream fis3 = fis2; FileInputStream fis4 = null) {\n" +
				"		fis = new FileInputStream(\"\");\n" +
				"		fis2.available();\n" +	// may be null since fis may be null
				"		fis3.close();\n" +
				"		fis4.available();\n" +	// will always be null
				"		m(1);\n" +
				"    } catch (IllegalArgumentException e) {\n" +
				"      fis.available();\n" + // may be null
				"    } catch (MyException e) {\n" +
				"      fis.available();\n" + // cannot be null
				"    } finally {}\n" +
				"  }\n" +
				"}\n"},
		"""
			----------
			1. WARNING in X.java (at line 4)
				class MyException extends Exception {}
				      ^^^^^^^^^^^
			The serializable class MyException does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 12)
				fis = new FileInputStream("");
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Resource leak: 'fis' is never closed
			----------
			3. ERROR in X.java (at line 13)
				fis2.available();
				^^^^
			Potential null pointer access: The variable fis2 may be null at this location
			----------
			4. ERROR in X.java (at line 14)
				fis3.close();
				^^^^
			Potential null pointer access: The variable fis3 may be null at this location
			----------
			5. ERROR in X.java (at line 15)
				fis4.available();
				^^^^
			Null pointer access: The variable fis4 can only be null at this location
			----------
			6. ERROR in X.java (at line 18)
				fis.available();
				^^^
			Potential null pointer access: The variable fis may be null at this location
			----------
			""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis - throw
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201182
public void test0595_throw() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) throws Throwable {
				    Throwable t = null;
				    throw t;
				  }
				}
				"""
			},
		true /* expectingCompilerErrors */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				throw t;
				      ^
			Null pointer access: The variable t can only be null at this location
			----------
			""",
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// null analysis - throw
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201182
// variant - potential NPE
public void test0596_throw() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) throws Throwable {
				    Throwable t = null;
				    if (args.length > 0) {
				      t = new Throwable();
				    }
				    throw t;
				  }
				}
				"""
			},
		true /* expectingCompilerErrors */,
		"""
			----------
			1. ERROR in X.java (at line 7)
				throw t;
				      ^
			Potential null pointer access: The variable t may be null at this location
			----------
			""",
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}


// null analysis - throw
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201182
// variant - unknown
public void test0597_throw() {
	runTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() throws Throwable {
				    throw t();
				  }
				  Throwable t() {
				    return new Throwable();
				  }
				}
				"""
			},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		"" /* expectedErrorString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// null analysis -- do while
public void test0601_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {/* */}\n" +
			"    while (o.toString() != null);\n" +
			      // complain: NPE
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				while (o.toString() != null);
				       ^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0602_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {/* */}\n" +
			"    while (o != null);\n" +
			  // complain: get o null first time and forever
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				while (o != null);
				       ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0603_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    while (o == null);\n" +
			      // complain: set it to non null before test, for each iteration
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				while (o == null);
				       ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0604_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    do {
				      if (System.currentTimeMillis() > 10L) {
				        o = new Object();
				      }
				    }
				    while (o == null);
				  }
				}
				"""},
		"");
}

// null analysis -- do while
public void test0605_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo(Object o) {
				    o = null;
				    do {
				      // do nothing
				    }
				    while (dummy || o != null);
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 8)
				while (dummy || o != null);
				                ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0606_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null,
				           u = new Object(),
				           v = new Object();
				    do {
				      if (v == null) {
				        o = new Object();
				      };
				      if (u == null) {
				        v = null;
				      };
				      u = null;
				    }
				    while (o == null);
				  }
				}
				"""},
		"");
}

// null analysis -- do while
public void test0607_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    do {\n" +
			"      o.toString();\n" +
			         // complain: NPE
			"      o = new Object();\n" +
			"    }\n" +
			"    while (dummy);\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- do while
public void test0608_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo() {
				    Object o = null;
				    do {
				      o = new Object();
				    }
				    while (dummy);
				    o.toString();
				  }
				}
				"""},
		"");
}

// null analysis -- do while
public void test0609_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  void foo() {
				    Object o = null;
				    do { /* */ }
				    while (dummy);
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - do while
public void test0610_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  X bar() {\n" +
			"    return null;\n" +
			"  }\n" +
			"  void foo(X x) {\n" +
			"    x.bar();\n" +
			"    do {\n" +
			"      x = x.bar();\n" +
			"    } while (x != null);\n" + // quiet
			"  }\n" +
			"}\n"},
		"");
}

// null analysis - do while
public void test0611_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  X bar() {\n" +
			"    return new X();\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    do {\n" +
			"      o = bar();\n" +
			"    } while (o == null);\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// the problem here is that a single pass cannot know for the return
// embedded into the if; prior approach did use the upstream flow
// info to catch this, but this is inappropriate in many cases (eg
// test0606)
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
public void _test0612_do_while() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object doubt) {
				    Object o = null;
				    do {
				      if (o == null) {
				        return;
				      }
				      o = doubt;
				    } while (true);
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			"""
	);
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147118
public void test0613_do_while() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  String f;
				  void foo (boolean b) {
				    X x = new X();
				    do {
				      System.out.println(x.f);
				      if (b) {
				        x = null;
				      }
				    } while (x != null);
				  }
				}
				"""},
		"");
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant
public void _test0614_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object doubt) {
				    Object o = null;
				    exit: do {
				      if (o == null) {
				        continue exit;
				      }
				      o = doubt;
				    } while (true);
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123399
// variant
public void _test0615_do_while() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object doubt) {
				    Object o = null;
				    do {
				      if (o == null) {
				        throw new RuntimeException();
				      }
				      o = doubt;
				    } while (true);
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0616_do_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(int i) {
				    Object o = null;
				    done: do {
				      switch (i) {
				        case 0:
				          o = new Object();
				          break;
				        case 1:
				          break done;
				      }
				    } while (true);
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0617_do_while_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean test() {
				    return true;
				  }
				  void foo() {
				    Object o = null;
				    done: do {
				      if (test()) {
				        break done;
				      }
				      o = new Object();
				    } while (true);
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0618_do_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    do {
				      if (a[0] == null)
				        break;
				      key = a[0];
				    } while (true);
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis - do while
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0619_do_while_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    loop: do {
				      if (a[0] == null)
				        break loop;
				      key = a[0];
				    } while (true);
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis -- for
public void test0701_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o.toString() != null;) {/* */}\n" +
			      // complain: NPE
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				for (;o.toString() != null;) {/* */}
				      ^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0702_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o != null;) {/* */}\n" +
			  // complain: get o null first time and forever
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				for (;o != null;) {/* */}
				      ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0703_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o == null;) {\n" +
			      // quiet: first iteration is sure to find it null,
			      // but other iterations may change it
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0704_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object o = null;\n" +
			"    for (;o == null;) {\n" +
			     // quiet: first iteration is sure to find it null,
			     // but other iterations may change it
			"      if (System.currentTimeMillis() > 10L) {\n" +
			"        o = new Object();\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0705_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean bar() {\n" +
			"    return true;\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    for (;bar() && o == null;) {\n" +
			"      o.toString();\n" + // complain: NPE because of condition
			"      o = new Object();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0707_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    for (;o == null; o.toString()) {
				      o = new Object();
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- for
public void test0708_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    for (;o == null; o.toString()) {
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 3)
				for (;o == null; o.toString()) {
				                 ^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0709_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    for (o.toString(); o == null;) { /* */ }\n" + // complain: protected then unchanged
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 3)
				for (o.toString(); o == null;) { /* */ }
				                   ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0710_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean bar() {
				    return true;
				  }
				  void foo(Object o) {
				    o = null;
				    for (o.toString(); bar();) {
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				for (o.toString(); bar();) {
				     ^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0711_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object t[] = null;\n" +
				"    for (Object o : t) {/* */}\n" +
				      // complain: NPE
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					for (Object o : t) {/* */}
					                ^
				Null pointer access: The variable t can only be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0712_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Iterable i = null;\n" +
				"    for (Object o : i) {/* */}\n" +
				      // complain: NPE
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					for (Object o : i) {/* */}
					                ^
				Null pointer access: The variable i can only be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0713_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo() {
					    Object t[] = new Object[1];
					    for (Object o : t) {/* */}
					  }
					}
					"""},
			"");
	}
}

// null analysis -- for
public void test0714_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo() {
					    Iterable i = new java.util.Vector<Object>();
					    for (Object o : i) {/* */}
					  }
					}
					"""},
			"");
	}
}

// null analysis -- for
public void test0715_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Iterable i = new java.util.Vector<Object>();\n" +
				"    Object flag = null;\n" +
				"    for (Object o : i) {\n" +
				"      flag = new Object();\n" +
				"    }\n" +
				"    flag.toString();\n" +
				// complain: cannot know if at least one iteration got executed
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 8)
					flag.toString();
					^^^^
				Potential null pointer access: The variable flag may be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0716_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo() {
					    Iterable i = new java.util.Vector<Object>();
					    Object flag = null;
					    for (Object o : i) { /* */ }
					    flag.toString();
					  }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 6)
					flag.toString();
					^^^^
				Null pointer access: The variable flag can only be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0717_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(boolean dummy) {
					    Object flag = null;
					    for (;dummy;) {
					      flag = new Object();
					    }
					    flag.toString();
					  }
					}
					"""},
			"""
				----------
				1. ERROR in X.java (at line 7)
					flag.toString();
					^^^^
				Potential null pointer access: The variable flag may be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- for
public void test0718_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(boolean dummy) {
				    Object flag = null;
				    for (;dummy;) { /* */ }
				    flag.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				flag.toString();
				^^^^
			Null pointer access: The variable flag can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
// origin: AssignmentTest#test019
public void test0719_for() {
	this.runConformTest(
		new String[] {
			    "X.java",
			    """
					public class X {
					  public static final char[] foo(char[] a, char c1, char c2) {
					   char[] r = null;
					   for (int i = 0, length = a.length; i < length; i++) {
					     char c = a[i];
					     if (c == c1) {
					       if (r == null) {
					         r = new char[length];
					       }
					       r[i] = c2;
					     } else if (r != null) {
					       r[i] = c;
					     }
					   }
					   if (r == null) return a;
					   return r;
					 }
					}
					"""},
		"");
}

// null analysis -- for
public void test0720_for_continue_break() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo() {\n" +
			  "    Object o = new Object();\n" +
			  "    for (int i = 0; i < 10; i++) {\n" +
			  "      if (o == null) {\n" + // complain: o cannot be null
			  "        continue;\n" +
			  "      }\n" +
			  "      o = null;\n" +
			  "      break;\n" +
			  "    }\n" +
			  "  }\n" +
			  "}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o == null) {
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0721_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    for (; b ? (o = new Object()).equals(o) : false ;) {\n" +
			// contrast this with test0238; here the condition shades doubts
			// upon o being null
			"      /* */\n" +
			"    }\n" +
			"    if (o == null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- for
public void test0722_for_return() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo (boolean b) {\n" +
			"    Object o = null;\n" +
			"    for (int i = 0; i < 25; i++) {\n" +
			"      if (b) {\n" +
			"        if (o == null) {\n" +
			"          o = new Object();\n" + // cleared by return downstream
			"        }\n" +
			"        return;\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- for
public void test0723_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo () {
				    Object o[] = new Object[1];
				    for (int i = 0; i < 1; i++) {
				      if (i < 1) {
				        o[i].toString();
				      }
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- for
public void test0724_for_with_initialization() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  X field;
				  void foo(X x1) {
				    // X x2;
				    outer: for (int i = 0; i < 30; i++) {
				      X x2 = x1;
				      do {
				        if (x2.equals(x1)) {
				          continue outer;
				        }
				        x2 = x2.field;
				      } while (x2 != null);
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- for
public void test0725_for_with_assignment() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  X field;
				  void foo(X x1) {
				    X x2;
				    outer: for (int i = 0; i < 30; i++) {
				      x2 = x1;
				      do {
				        if (x2.equals(x1)) {
				          continue outer;
				        }
				        x2 = x2.field;
				      } while (x2 != null);
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- for
// changed with https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
// we are now able to see that x2 is reinitialized with x1, which is unknown
public void test0726_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x1) {
				    X x2 = null;
				    for (int i = 0; i < 5; i++) {
				      if (x2 == null) {
				        x2 = x1;
				      }
				      x2.toString();
				    }
				  }
				}
				"""},
		"");
}

// null analysis -- for
public void test0727_for() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    for (; true;) { /* */ }
				  }
				}
				"""},
		"");
}

// null analysis -- for
public void test0728_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(X x) {\n" +
			"    for (; true; x.toString()) { /* */ }\n" +
			"    if (x == null) { /* */ }\n" + // complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (x == null) { /* */ }
				^^^^^^^^^^^^^^^^^^^^^^^^
			Unreachable code
			----------
			""");
}

// null analysis -- for
public void test0729_for_try_catch_finally() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				class X {
				  X f;
				  void bar() throws IOException {
				    throw new IOException();
				  }
				  void foo(boolean b) {
				    for (int i = 0 ; i < 5 ; i++) {
				      X x = this.f;
				      if (x == null) {\s
				        continue;
				      }
				      if (b) {
				        try {
				          bar();
				        }\s
				        catch(IOException e) { /* */ }
				        finally {
				          x.toString();
				        }
				      }
				    }
				  }
				}"""},
		"");
}

// null analysis - for
public void test0730_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  void foo(Object o) {\n" +
			"    for ( ; o == null ; ) {\n" +
			"      o = new Object();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - for
public void test0731_for() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  X bar() {\n" +
			"    return new X();\n" +
			"  }\n" +
			"  void foo(Object o) {\n" +
			"    for ( ; o == null ; ) {\n" +
			"      o = bar();\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" + // complain
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (o != null) { /* */ }
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - for nested with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
public void test0732_for_nested_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(String doubt) {
				    for(int i = 0; i < 10; i++) {
				      String s = doubt;
				      if(s != null) {
				        for(int j = 0; j < 1; j++) {
				          break;
				        }
				        s.length();
				      }
				    }
				  }
				}
				
				"""},
		"");
}

// null analysis - for while with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
// variant
public void test0733_for_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(String doubt, boolean b) {
				    for(int i = 0; i < 10; i++) {
				      String s = doubt;
				      if (s != null) {
				        while (b) {
				          break;
				        }
				        s.length();
				      }
				    }
				  }
				}
				
				"""},
		"");
}

// null analysis - for while with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
// variant
public void test0734_for_while_break() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(String doubt, boolean b) {
				    for(int i = 0; i < 10; i++) {
				      String s = doubt;
				      if (s != null) {
				        do {
				          break;
				        } while (b);
				        s.length();
				      }
				    }
				  }
				}
				
				"""},
		"");
}

// null analysis - for nested with break
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129371
// variant
public void test0735_for_nested_break() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  void foo(Object[] a, String doubt) {
					    for(int i = 0; i < 10; i++) {
					      String s = doubt;
					      if(s != null) {
					        for(Object o : a) {
					          break;
					        }
					        s.length();
					      }
					    }
					  }
					}
					
					"""},
			"");
	}
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
public void test0736_for_embedded_lazy_init() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public boolean foo() {\n" +
			"    Boolean b = null;\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (b == null) {\n" +
			"        b = Boolean.TRUE;\n" +
			"      }\n" +
			"      if (b.booleanValue()) {\n" + // quiet
			"        return b.booleanValue();\n" +
			"      }\n" +
			"    }\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis - for with unknown protection: unknown cannot protect anything
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
public void test0737_for_unknown_protection() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public boolean foo(Boolean p) {\n" +
			"    Boolean b = null;\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (b == p) {\n" + // tells us that p is null as well
			"        // empty\n" +
			"      }\n" +
			"      else {\n" +
			"        continue;\n" +
			"      }\n" +
			"      if (b.booleanValue()) {\n" + // complain b can only be null
			"        return b.booleanValue();\n" +
			"      }\n" +
			"    }\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 11)
				if (b.booleanValue()) {
				    ^
			Null pointer access: The variable b can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis - for with unknown protection
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=127570
// the issue is that we cannot do less than full aliasing analysis to
// catch this one
// PREMATURE (maxime) reconsider when/if we bring full aliasing in
public void _test0738_for_unknown_protection() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"  public boolean foo(Boolean p) {\n" +
			"    Boolean b = null;\n" +
			"    for (int i = 0; i < 1; i++) {\n" +
			"      if (b == p) {\n" +
			"        // empty\n" +
			"      }\n" +
			"      else {\n" +
			"        b = p;\n" +
			"      }\n" +
			"      if (b.booleanValue()) {\n" + // quiet because b is an alias for p, unknown
			"        return b.booleanValue();\n" +
			"      }\n" +
			"    }\n" +
			"    return false;\n" +
			"  }\n" +
			"}"},
		"");
}

// null analysis -- for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=178895
public void test0739_for() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					  void foo(List<Object> l, boolean b) {
					    for (Object o : l) {
					      if (b) {
					        if (o != null) {
					          return;
					        }
					      } else {
					        o.toString();
					      }
					    }
					  }
					}
					"""},
			"");
	}
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0740_for_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(int i) {
				    Object o = null;
				    done: for (;;) {
				      switch (i) {
				        case 0:
				          o = new Object();
				          break;
				        case 1:
				          break done;
				      }
				    }
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0741_for_explicit_label() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean test() {
				    return true;
				  }
				  void foo() {
				    Object o = null;
				    done: for (;;) {
				      if (test()) {
				        break done;
				      }
				      o = new Object();
				    }
				    if (o == null) {
				    }
				  }
				}
				"""},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=176472
// variant
public void test0742_for_explicit_label() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
					  void foo(int i, List<Object> l) {
					    Object o = null;
					    done: for (Object j: l) {
					      switch (i) {
					        case 0:
					          o = new Object();
					          break;
					        case 1:
					          break done;
					      }
					    }
					    if (o == null) {
					    }
					  }
					}
					"""},
			"");
	}
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
public void test0743_for_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    for( int i = 0; ; i++ )
				    {
				      if (a[i] == null)
				        break;
				      key = a[i];
				    }
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=184298
// variant
public void test0744_for_infinite() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public void test(String[] a) {
				    String key = null;
				    loop: for( int i = 0; ; i++ )
				    {
				      if (a[i] == null)
				        break loop;
				      key = a[i];
				    }
				    if (key != null) {
				      // empty
				    }
				  }
				}"""},
		"");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195638
public void test0746_for_try_catch() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    String str = null;
				    for (int i = 0; i < 2; i++) {
				      try {
				        str = new String("Test");
				      } catch (Exception ex) {
				        ex.printStackTrace();
				      }
				      str.charAt(i);
				      str = null;
				    }
				  }
				}
				"""
			},
		"""
			----------
			1. ERROR in X.java (at line 10)
				str.charAt(i);
				^^^
			Potential null pointer access: The variable str may be null at this location
			----------
			""");
}

// null analysis - for
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=195638
// variant: do not reset to null
public void test0747_for_try_catch() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    String str = null;
				    for (int i = 0; i < 2; i++) {
				      try {
				        str = new String("Test");
				      } catch (Exception ex) {
				        ex.printStackTrace();
				      }
				      str.charAt(i);
				    }
				  }
				}
				"""
			},
		"""
			----------
			1. ERROR in X.java (at line 10)
				str.charAt(i);
				^^^
			Potential null pointer access: The variable str may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0800_switch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o = new Object();\n" +
			"       break;\n" +
			"     case 2 :\n" +
			"       return;\n" +
			"   }\n" +
			"   if(o == null) { /* */ }\n" + // quiet: don't know whether came from 0 or default
			" }\n" +
			"}\n"},
		"");
}

// null analysis -- switch
public void test0801_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o = new Object();\n" +
			"       break;\n" +
			"     default :\n" +
			"       return;\n" +
			"   }\n" +
			"   if(o == null) { /* */ }\n" + // complain: only get there through 0, o non null
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				if(o == null) { /* */ }
				   ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 12)
				if(o == null) { /* */ }
				              ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0802_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o.toString();\n" + // complain: o can only be null
			"       break;\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 7)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0803_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" int k;\n" +
			" void foo() {\n" +
			"   Object o = null;\n" +
			"   switch (k) {\n" +
			"     case 0 :\n" +
			"       o = new Object();\n" +
			"     case 1 :\n" +
			"       o.toString();\n" + // complain: may come through 0 or 1
			"       break;\n" +
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0804_switch() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo (Object o, int info) {\n" +
			"	 o = null;\n" +
			"	 switch (info) {\n" +
			"	   case 0 :\n" +
			"		 o = new Object();\n" +
			"		 break;\n" +
			"	   case 1 :\n" +
			"		 o = new String();\n" +
			"		 break;\n" +
			"	   default :\n" +
			"		 o = new X();\n" +
			"		 break;\n" +
			"	 }\n" +
			"	 if(o != null) { /* */ }\n" + // complain: all branches allocate a new o
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 15)
				if(o != null) { /* */ }
				   ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- switch
public void test0805_switch() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X p) {
				    X x = this;
				    for (int i = 0; i < 5; i++) {
				      switch (i) {
				        case 1:
				          x = p;
				      }
				    }
				    if (x != null) { /* */ }
				  }
				}
				"""},
		"");
}

// null analysis -- non null protection tag
public void _test0900_non_null_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    o/*NN*/.toString();\n" + 	// protection => do not complain
			"    o.toString();\n" + 		// protected by previous line
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- non null protection tag
public void _test0901_non_null_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, boolean b) {
				    if (b) {
				      o = null;
				    }
				    o/*NN*/.toString();
				    if (b) {
				      o = null;
				    }
				    o/*
				         NN  comment  */.toString();
				    if (b) {
				      o = null;
				    }
				    o/*  NN
				               */.toString();
				    if (b) {
				      o = null;
				    }
				    o               //  NN  \s
				      .toString();
				  }
				}
				"""},
		"");
}

// null analysis -- non null protection tag
public void _test0902_non_null_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, boolean b) {
				    if (b) {
				      o = null;
				    }
				    o/*NON-NULL*/.toString();
				    if (b) {
				      o = null;
				    }
				    o/*  NON-NULL   comment */.toString();
				    if (b) {
				      o = null;
				    }
				    o/*  NON-NULL  \s
				               */.toString();
				    if (b) {
				      o = null;
				    }
				    o               //  NON-NULL  \s
				      .toString();
				  }
				}
				"""},
		"");
}

// null analysis -- non null protection tag
public void test0903_non_null_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, boolean b) {
				    if (b) {
				      o = null;
				    }
				    o/*N N*/.toString();
				    if (b) {
				      o = null;
				    }
				    o/*NNa*/.toString();
				    if (b) {
				      o = null;
				    }
				    o/*aNN */.toString();
				    if (b) {
				      o = null;
				    }
				    o/*NON NULL*/.toString();
				    if (b) {
				      o = null;
				    }
				    o/*Non-Null*/.toString();
				    if (b) {
				      o = null;
				    }
				    o/*aNON-NULL */.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				o/*N N*/.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			2. ERROR in X.java (at line 10)
				o/*NNa*/.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			3. ERROR in X.java (at line 14)
				o/*aNN */.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			4. ERROR in X.java (at line 18)
				o/*NON NULL*/.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			5. ERROR in X.java (at line 22)
				o/*Non-Null*/.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			6. ERROR in X.java (at line 26)
				o/*aNON-NULL */.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}


// null analysis -- non null protection tag
public void test0905_non_null_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    o.toString();/*NN*/\n" + 	// too late to protect => complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();/*NN*/
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- non null protection tag
public void test0906_non_null_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(Object o) {\n" +
			"    boolean b = o != null;\n" + // shades doubts upon o
			"    /*NN*/o.toString();\n" + 	// too soon to protect => complain
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				/*NN*/o.toString();
				      ^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0950_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    boolean b = o != null;\n" + // shades doubts upon o
				"    assert(o != null);\n" + 	// protection
				"    o.toString();\n" + 		// quiet
				"  }\n" +
				"}\n"},
			"");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0951_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    assert(o == null);\n" + 	// forces null
				"    o.toString();\n" + 		// can only be null
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0952_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o, boolean b) {\n" +
				"    assert(o != null || b);\n" + // shade doubts
				"    o.toString();\n" + 		// complain
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0953_assert_combined() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o1, Object o2) {\n" +
				"    assert(o1 != null && o2 == null);\n" +
				"    if (o1 == null) { };\n" + 		// complain
				"    if (o2 == null) { };\n" + 		// complain
				"  }\n" +
				"}\n"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if (o1 == null) { };
					    ^^
				Null comparison always yields false: The variable o1 cannot be null at this location
				----------
				2. WARNING in X.java (at line 4)
					if (o1 == null) { };
					                ^^^
				Dead code
				----------
				3. ERROR in X.java (at line 5)
					if (o2 == null) { };
					    ^^
				Redundant null check: The variable o2 can only be null at this location
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0954_assert_fake_reachable() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		runConformTest(
			true/*flush*/,
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    assert(false && o != null);\n" +
				"    if (o == null) { };\n" + 		// quiet
				"  }\n" +
				"}\n"
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					assert(false && o != null);
					                ^^^^^^^^^
				Dead code
				----------
				""",
			"",
			"",
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0955_assert_combined() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"    assert(false || o != null);\n" +
				"    if (o == null) { };\n" + 		// complain
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o == null) { };
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o == null) { };
				               ^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=127244
// [compiler] Null reference analysis doesn't understand assertions
public void test0956_assert_combined() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    Object o = null;\n" +
				"    assert(o != null);\n" +    // complain
				"    if (o == null) { };\n" +   // complain
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				assert(o != null);
				       ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 5)
				if (o == null) { };
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			3. WARNING in X.java (at line 5)
				if (o == null) { };
				               ^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings,
// but this doesn't affect the downstream info.
public void test0957_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"	 assert (foo != null);\n" +	//don't warn
				"	 if (foo == null) {}\n" +
				"    X foo2 = new X();\n" +
				"	 assert (foo2 == null);\n" +	//don't warn
				"	 if (foo2 == null) {}\n" +
				"    X bar = null;\n" +
				"	 assert (bar == null);\n" +	//don't warn
				"	 if (bar == null) {}\n" +
				"    X bar2 = null;\n" +
				"	 assert (bar2 != null);\n" +	//don't warn
				"	 if (bar2 == null) {}\n" +
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 5)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 7)
				assert (foo2 == null);
				        ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 8)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 11)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 13)
				assert (bar2 != null);
				        ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 14)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 14)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from null comparison warnings,
// but this doesn't affect the downstream info.
public void test0958_assert() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"public class X {\n" +
				"  void m() {\n" +
				"    HashMap<Integer,X> map = new HashMap<Integer,X>();\n" +
				"	 X bar = null;\n" +
				"    X foo = map.get(1);\n" +
				"    if (foo == null) {\n" +
				"	 	foo = new X();\n" +
				"		map.put(1, foo);\n" +
				"	 }\n" +
				"	 assert (foo != null && bar == null);\n" +	// don't warn but do the null analysis
				"	 if (foo != null) {}\n" +		// warn
				"	 if (bar == null) {}\n" +		// warn
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				if (foo != null) {}
				    ^^^
			Redundant null check: The variable foo cannot be null at this location
			----------
			2. ERROR in X.java (at line 13)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void test0959a_assert_loop() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"    X foo2 = new X();\n" +
				"    X bar = null;\n" +
				"    X bar2 = null;\n" +
				"	 while (true) {\n" +
				"	 	assert (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"	 	assert (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +
				"	 	assert (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +
				"	 	assert (bar2 != null);\n" +	//don't warn
				"	 	if (bar2 == null) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 9)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 10)
				assert (foo2 == null);
				        ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 11)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 13)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 14)
				assert (bar2 != null);
				        ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 15)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 15)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings in a looping context,
// but this doesn't affect the downstream info.
public void test0959b_assert_loop() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"	 while (true) {\n" +
				"   	X foo = new X();\n" +
				"	 	assert (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"    	X foo2 = new X();\n" +
				"	 	assert (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +
				"    	X bar = null;\n" +
				"	 	assert (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +
				"    	X bar2 = null;\n" +
				"	 	assert (bar2 != null);\n" +	//don't warn
				"	 	if (bar2 == null) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 6)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 8)
				assert (foo2 == null);
				        ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 9)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 12)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 14)
				assert (bar2 != null);
				        ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 15)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 15)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=250056
// Test to verify that asserts are exempted from redundant null check warnings in a finally context,
// but this doesn't affect the downstream info.
public void test0960_assert_finally() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void m() {\n" +
				"    X foo = new X();\n" +
				"    X foo2 = new X();\n" +
				"    X bar = null;\n" +
				"    X bar2 = null;\n" +
				"	 try {\n" +
				"		System.out.println(\"Inside try\");\n" +
				"	 }\n" +
				"	 finally {\n" +
				"	 	assert (foo != null);\n" +	//don't warn
				"	 	if (foo == null) {}\n" +
				"	 	assert (foo2 == null);\n" +	//don't warn
				"	 	if (foo2 == null) {}\n" +
				"	 	assert (bar == null);\n" +	//don't warn
				"	 	if (bar == null) {}\n" +
				"	 	assert (bar2 != null);\n" +	//don't warn
				"	 	if (bar2 == null) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 12)
				if (foo == null) {}
				    ^^^
			Null comparison always yields false: The variable foo cannot be null at this location
			----------
			2. WARNING in X.java (at line 12)
				if (foo == null) {}
				                 ^^
			Dead code
			----------
			3. ERROR in X.java (at line 13)
				assert (foo2 == null);
				        ^^^^
			Null comparison always yields false: The variable foo2 cannot be null at this location
			----------
			4. ERROR in X.java (at line 14)
				if (foo2 == null) {}
				    ^^^^
			Redundant null check: The variable foo2 can only be null at this location
			----------
			5. ERROR in X.java (at line 16)
				if (bar == null) {}
				    ^^^
			Redundant null check: The variable bar can only be null at this location
			----------
			6. ERROR in X.java (at line 17)
				assert (bar2 != null);
				        ^^^^
			Null comparison always yields false: The variable bar2 can only be null at this location
			----------
			7. ERROR in X.java (at line 18)
				if (bar2 == null) {}
				    ^^^^
			Null comparison always yields false: The variable bar2 cannot be null at this location
			----------
			8. WARNING in X.java (at line 18)
				if (bar2 == null) {}
				                  ^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// null analysis -- notNull protection tag
public void _test0900_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(/** @notNull */ Object o) {
				    boolean b = o != null;
				  }
				}
				"""},
		"ERR cannot be null");
}

// null analysis -- notNull protection tag
public void _test0901_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o) {
				    /** @notNull */ Object l = o;
				  }
				}
				"""},
		"ERR cannot be null... ou pas ?");
}

// null analysis -- notNull protection tag
public void _test0902_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(/** @nullable */ Object o) {
				    /** @notNull */ Object l = o;
				  }
				}
				"""},
		"ERR cannot be null");
}

// null analysis -- notNull protection tag
public void test0903_notNull_protection_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object bar() {
				    return null;
				  }
				  void foo() {
				    /** @notNull */ Object l = bar();
				  }
				}
				"""},
		"");
}

// null analysis -- notNull protection tag
public void _test0904_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /** @notNull */
				  Object bar() {
				    return new Object();
				  }
				  void foo() {
				    Object l = bar();
				    if (l == null) { /* empty */ }
				  }
				}
				"""},
		"ERR cannot be null");
}

// null analysis -- notNull protection tag
public void _test0905_notNull_protection_tag() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /** @notNull */
				  Object bar() {
				    return null;
				  }
				}
				"""},
		"ERR cannot be null");
}

// null analysis -- nullable tag
public void _test0950_nullable_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(/** @nullable */ Object o) {
				    o.toString();
				  }
				}
				"""},
		"ERR may be null");
}

// null analysis -- nullable tag
public void _test0951_nullable_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(/** @nullable */ Object o) {
				    Object l = o;
				    l.toString();
				  }
				}
				"""},
		"ERR may be null");
}

// null analysis -- nullable tag
public void _test0952_nullable_tag() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(boolean b) {
				    /** @nullable */ Object o;
				    if (b) {
				      o = new Object();
				    }
				    o.toString();
				  }
				}
				"""},
		"ERR may be null");
}

// moved from AssignmentTest
public void test1004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  X foo(X x) {
				    x.foo(null); // 0
				    if (x != null) { // 1
				      if (x == null) { // 2
				        x.foo(null); // 3
				      } else if (x instanceof X) { // 4
				        x.foo(null); // 5\s
				      } else if (x != null) { // 6
				        x.foo(null); // 7
				      }
				      x.foo(null); // 8
				    }
				    return this;
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (x != null) { // 1
				    ^
			Redundant null check: The variable x cannot be null at this location
			----------
			2. ERROR in X.java (at line 5)
				if (x == null) { // 2
				    ^
			Null comparison always yields false: The variable x cannot be null at this location
			----------
			3. WARNING in X.java (at line 5)
				if (x == null) { // 2
			        x.foo(null); // 3
			      } else if (x instanceof X) { // 4
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			4. ERROR in X.java (at line 9)
				} else if (x != null) { // 6
				           ^
			Redundant null check: The variable x cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1005() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Class c) {
				    if (c.isArray() ) {
				    } else if (c == java.lang.String.class ) {
				    }
				  }
				}
				"""},
		"");
}

public void test1006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x) {
				    if (x == this)
				     return;
				    x.foo(this);
				  }
				}
				"""},
		"");
}

public void test1007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x, X x2) {
				    if (x != null)
				      return;
				    x = x2;
				    if (x == null) {
				    }
				  }
				}
				"""},
		"");
}

public void test1008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x, X x2) {
				    if (x != null)
				      return;
				    try {
				      x = x2;
				    } catch(Exception e) {}
				    if (x == null) {
				    }
				  }
				}
				"""},
		"");
}

public void test1009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				
				public class X {
				  boolean check(String name) { return true; }
				  Class bar(String name) throws ClassNotFoundException { return null; }
				  File baz(String name) { return null; }
				 \s
				  public Class foo(String name, boolean resolve) throws ClassNotFoundException {
				   \s
				    Class c = bar(name);
				    if (c != null)
				      return c;
				    if (check(name)) {
				      try {
				        c= bar(name);
				          return c;
				      } catch (ClassNotFoundException e) {
				        // keep searching
				        // only path to here left c unassigned from try block, means it was assumed to be null
				      }
				    }
				    if (c == null) {// should complain: c can only be null
				      File file= baz(name);
				      if (file == null)
				        throw new ClassNotFoundException();
				    }
				    return c;
				  }
				
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 22)
				if (c == null) {// should complain: c can only be null
				    ^
			Redundant null check: The variable c can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				  X itself() { return this; }
				
				  void bar() {
				    X itself = this.itself();
				    if (this == itself) {
				      System.out.println(itself.toString()); //1
				    } else {
				      System.out.println(itself.toString()); //2
				    }
				  }
				}
				"""},
		"");
}

public void test1011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				  X itself() { return this; }
				
				  void bar() {
				    X itself = this.itself();
				    if (this == itself) {
				      X other = (X)itself;
				      if (other != null) {
				      }
				      if (other == null) {
				      }
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (other != null) {
				    ^^^^^
			Redundant null check: The variable other cannot be null at this location
			----------
			2. ERROR in X.java (at line 11)
				if (other == null) {
				    ^^^^^
			Null comparison always yields false: The variable other cannot be null at this location
			----------
			3. WARNING in X.java (at line 11)
				if (other == null) {
			      }
				                   ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  void foo() {
				    Object o = null;
				    do {
				      if (o == null) {
				        return;
				      }
				      // o = bar();
				    } while (true);
				  }
				  X bar() {\s
				    return null;\s
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// x cannot equal this then null with no assignment in between
// each diagnostic is locally sound though
public void test1013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x) {
				    if (x == this) {
				      if (x == null) {
				        x.foo(this);
				      }
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (x == null) {
				    ^
			Null comparison always yields false: The variable x cannot be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (x == null) {
			        x.foo(this);
			      }
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x) {
				    x = null;
				    try {
				      x = this;
				    } finally {
				      x.foo(null);
				    }
				  }
				}
				"""},
		"");
}

public void test1015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    int i = 1;
				    switch (i) {
				      case 1:
				        o = new Object();
				        break;
				    }
				    if (o != null)
				      o.toString();
				  }
				}
				"""},
		"");
}

public void test1016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x) {
				    x = null;
				    try {
				      x = null;
				    } finally {
				      if (x != null) {
				        x.foo(null);
				      }
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				x = null;
				^
			Redundant assignment: The variable x can only be null at this location
			----------
			2. ERROR in X.java (at line 7)
				if (x != null) {
				    ^
			Null comparison always yields false: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(X x) {
				    x = this;
				    try {
				      x = null;
				    } finally {
				      if (x == null) {
				        x.foo(null);
				      }
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (x == null) {
				    ^
			Redundant null check: The variable x can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				x.foo(null);
				^
			Null pointer access: The variable x can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  void foo() {
				    Object o = null;
				    do {
				      if (o != null) return;
				      o = null;
				    } while (true);
				  }
				  X bar() {\s
				    return null;\s
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o != null) return;
				    ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 7)
				o = null;
				^
			Redundant assignment: The variable o can only be null at this location
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static final char[] replaceOnCopy(
				      char[] array,
				      char toBeReplaced,
				      char replacementChar) {
				     \s
				    char[] result = null;
				    for (int i = 0, length = array.length; i < length; i++) {
				      char c = array[i];
				      if (c == toBeReplaced) {
				        if (result == null) {
				          result = new char[length];
				          System.arraycopy(array, 0, result, 0, i);
				        }
				        result[i] = replacementChar;
				      } else if (result != null) {
				        result[i] = c;
				      }
				    }
				    if (result == null) return array;
				    return result;
				  }
				}
				"""},
		"");
}

public void test1021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int kind;
				  X parent;
				  Object[] foo() { return null; }
				  void findTypeParameters(X scope) {
				    Object[] typeParameters = null;
				    while (scope != null) {
				      typeParameters = null;
				      switch (scope.kind) {
				        case 0 :
				          typeParameters = foo();
				          break;
				        case 1 :
				          typeParameters = foo();
				          break;
				        case 2 :
				          return;
				      }
				      if(typeParameters != null) {
				        foo();
				      }
				      scope = scope.parent;
				    }
				  }
				}
				"""},
		"");
}

public void test1022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean bool() { return true; }
				  void doSomething() {}
				 \s
				  void foo() {
				    Object progressJob = null;
				    while (bool()) {
				      if (bool()) {
				        if (progressJob != null)
				          progressJob = null;
				        doSomething();
				      }
				      try {
				        if (progressJob == null) {
				          progressJob = new Object();
				        }
				      } finally {
				        doSomething();
				      }
				    }
				  }
				}"""},
		"");
}

public void test1023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				  void foo(Object that) {
				    Object o = new Object();
				    while (that != null) {
				      try {
				        o = null;
				        break;
				      } finally {
				        o = new Object();
				      }
				    }
				    if (o == null) return;
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 13)
				if (o == null) return;
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 13)
				if (o == null) return;
				               ^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  boolean bool() { return true; }
				  void doSomething() {}
				 \s
				  void foo() {
				    Object progressJob = null;
				    while (bool()) {
				      if (progressJob != null)
				        progressJob = null;
				      doSomething();
				      try {
				        if (progressJob == null) {
				          progressJob = new Object();
				        }
				      } finally {
				        doSomething();
				      }
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 13)
				if (progressJob == null) {
				    ^^^^^^^^^^^
			Redundant null check: The variable progressJob can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  void foo() {
				    Object o;
				    try {
				      o = null;
				    } finally {
				      o = new Object();
				    }
				    if (o == null) return;
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 10)
				if (o == null) return;
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 10)
				if (o == null) return;
				               ^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// TODO (philippe) reenable once fixed
public void _test1026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  public static void main(String[] args) {
				    Object o;
				    try {
				      o = null;
				    } finally {
				      if (args == null) o = new Object();
				    }
				    if (o == null) System.out.println("SUCCESS");
				  }
				}
				"""},
		"SUCCESS");
}

public void test1027() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo() {
				    Object o = null;
				    while (b) {
				      try {
				        o = null;
				      } finally {
				        if (o == null)\s
				          o = new Object();
				        }
				      }
				    if (o == null) return;
				  }
				}
				"""},
			"""
				----------
				1. ERROR in X.java (at line 9)
					if (o == null)\s
					    ^
				Redundant null check: The variable o can only be null at this location
				----------
				""");
}

// TODO (philippe) reenable once fixed
public void _test1028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean b;
				  void foo() {
				    Object o = null;
				    while (b) {
				      try {
				        o = null;
				        break;
				      } finally {
				        if (o == null)\s
				          o = new Object();
				      }
				    }
				    if (o == null) return;
				  }
				}
				"""},
		"");
}

public void test1029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    Object o = null;
				    int i = 0;
				    while (i++ < 2) {
				      try {
				        if (i == 2) return;
				        o = null;
				      } finally {
				        if (i == 2) System.out.println(o);
				        o = "SUCCESS";
				      }
				    }
				    if (o == null) return;
				  }
				}
				"""},
		"SUCCESS");
}

public void test1030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  void foo() {
				    Object a = null;
				    while (true) {
				      a = null;
				      if (a == null) {
				        System.out.println();
				      }
				      a = new Object();
				      break;
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				a = null;
				^
			Redundant assignment: The variable a can only be null at this location
			----------
			2. ERROR in X.java (at line 7)
				if (a == null) {
				    ^
			Redundant null check: The variable a can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  void foo() {
				    Object a = null;
				    while (true) {
				      a = null;
				      if (a == null) {
				        System.out.println();
				      }
				      a = new Object();
				      break;
				    }
				    if (a == null) {
				      System.out.println();
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				a = null;
				^
			Redundant assignment: The variable a can only be null at this location
			----------
			2. ERROR in X.java (at line 7)
				if (a == null) {
				    ^
			Redundant null check: The variable a can only be null at this location
			----------
			3. ERROR in X.java (at line 13)
				if (a == null) {
				    ^
			Null comparison always yields false: The variable a cannot be null at this location
			----------
			4. WARNING in X.java (at line 13)
				if (a == null) {
			      System.out.println();
			    }
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test1032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o1 = this;
				    Object o3;
				    while (o1 != null && (o3 = o1) != null) {
				      o1 = o3;
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				while (o1 != null && (o3 = o1) != null) {
				       ^^
			Redundant null check: The variable o1 cannot be null at this location
			----------
			2. ERROR in X.java (at line 5)
				while (o1 != null && (o3 = o1) != null) {
				                     ^^^^^^^^^
			Redundant null check: The variable o3 cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// (simplified to focus on nulls)
public void test1033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 \s
				  void foo() {
				    String a,b;
				    do{
				      a="Hello ";
				    }while(a!=null);
				    if(a!=null)
				      { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				}while(a!=null);
				       ^
			Redundant null check: The variable a cannot be null at this location
			----------
			2. ERROR in X.java (at line 8)
				if(a!=null)
				   ^
			Null comparison always yields false: The variable a can only be null at this location
			----------
			3. WARNING in X.java (at line 9)
				{ /* */ }
				^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// from AssignmentTest#test034, simplified
public void test1034() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public final class X\s
				{
					void foo()
					{
						String rs = null;
						try
						{
							rs = "";
							return;
						}
						catch (Exception e)
						{
						}
						finally
						{
							if (rs != null)
							{
								try
								{
									rs.toString();
								}
								catch (Exception e)
								{
								}
							}
						}
						return;
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 16)
				if (rs != null)
				    ^^
			Redundant null check: The variable rs cannot be null at this location
			----------
			""");
}

public void test1036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				  void foo() {
				    Object o = new Object();
				    do {
				      o = null;
				    } while (o != null);
				    if (o == null) {
				      // throw new Exception();
				    }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				} while (o != null);
				         ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. ERROR in X.java (at line 8)
				if (o == null) {
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// default for null options is Ignore
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=192875
// changed default for null access to warning
public void test1050_options_all_default() {
	try {
		setNullRelatedOptions = false;
		runConformTest(
			true, // flush
			new String[] {
				"X.java",
				  "public class X {\n" +
				  "  void foo(Object p) {\n" +
				  "    Object o = null;\n" +
				  "    if (o != null) {\n" +
				  "       o = null;\n" +
				  "    }\n" +
				  "    if (p == null) {}\n" + // taint p
				  "    o.toString();\n" +
				  "    p.toString();\n" +
				  "  }\n" +
				  "}\n"
				  } /* testFiles */,
			"""
				----------
				1. WARNING in X.java (at line 4)
					if (o != null) {
				       o = null;
				    }
					               ^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				2. WARNING in X.java (at line 8)
					o.toString();
					^
				Null pointer access: The variable o can only be null at this location
				----------
				""",
			"" /* expectedOutputString */,
			"" /* expectedErrorString */,
		    JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	}
	finally {
		setNullRelatedOptions = true;
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// all null options set to Ignore
public void test1051_options_all_ignore() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
    customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
			new String[] {
				"X.java",
				  "public class X {\n" +
				  "  void foo(Object p) {\n" +
				  "    Object o = null;\n" +
				  "    if (o != null) {\n" +
				  "       o = null;\n" +
				  "    }\n" +
				  "    if (p == null) {}\n" + // taint p
				  "    o.toString();\n" +
				  "    p.toString();\n" +
				  "  }\n" +
				  "}\n"},
			null /* no expected output string */,
			null /* no extra class libraries */,
			true /* flush output directory */,
			null /* no vm arguments */,
			customOptions,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// all options set to error
public void test1052_options_all_error() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = null;\n" +
			  "    if (o != null) {\n" +
			  "       o = null;\n" +
			  "    }\n" +
			  "    if (p == null) {}\n" + // taint p
			  "    o.toString();\n" +
			  "    p.toString();\n" +
			  "  }\n" +
			  "}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null) {
				    ^
			Null comparison always yields false: The variable o can only be null at this location
			----------
			2. WARNING in X.java (at line 4)
				if (o != null) {
			       o = null;
			    }
				               ^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 8)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			4. ERROR in X.java (at line 9)
				p.toString();
				^
			Potential null pointer access: The variable p may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1053_options_mix() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  void foo(Object p) {\n" +
			"    Object o = null;\n" +
			"    if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"    if (p == null) {}\n" + // taint p
			"    o.toString();\n" +
			"    p.toString();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1054_options_mix() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  void foo(Object p) {\n" +
			"    Object o = null;\n" +
			"    if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"    if (p == null) {}\n" + // taint p
			"    o.toString();\n" +
			"    p.toString();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 8)\n" +
		"	o.toString();\n" +
		"	^\n" +
		"Null pointer access: The variable o can only be null at this location\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1055_options_mix() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X {\n" +
			"  void foo(Object p) {\n" +
			"    Object o = null;\n" +
			"    if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"    if (p == null) {}\n" + // taint p
			"    o.toString();\n" +
			"    p.toString();\n" +
			"  }\n" +
			"}\n"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" +  /* expected compiler log */
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"	    ^\n" +
		"Null comparison always yields false: The variable o can only be null at this location\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	if (o != null) {\n" +
		"       o = null;\n" +
		"    }\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	p.toString();\n" +
		"	^\n" +
		"Potential null pointer access: The variable p may be null at this location\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
// selectively changing error levels
public void test1056_options_mix_with_SuppressWarnings() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"public class X {\n" +
				"@SuppressWarnings(\"null\")\n" +
				"  void foo(Object p) {\n" +
				"    Object o = null;\n" +
				"    if (o != null) {\n" +
				"       o = null;\n" +
				"    }\n" +
				"    if (p == null) {}\n" + // taint p
				"    o.toString();\n" +
				"    p.toString();\n" +
				"  }\n" +
				"}\n"
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			// compiler results
			"----------\n" +  /* expected compiler log */
			"1. WARNING in X.java (at line 5)\n" +
			"	if (o != null) {\n" +
			"       o = null;\n" +
			"    }\n" +
			"	               ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 9)\n" +
			"	o.toString();\n" +
			"	^\n" +
			"Null pointer access: The variable o can only be null at this location\n" +
			"----------\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test1057_options_instanceof_is_check() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = null;\n" +
			  "    if (p == null) {}\n" + // taint p
			  "    if (o instanceof String) {};\n" +
			  "    if (p instanceof String) {};\n" +
			  "  }\n" +
			  "}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o instanceof String) {};
				    ^
			instanceof always yields false: The variable o can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test1058_options_instanceof_is_check() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			  "public class X {\n" +
			  "  void foo(Object p) {\n" +
			  "    Object o = null;\n" +
			  "    if (p == null) {}\n" + // taint p
			  "    if (o instanceof String) {};\n" +
			  "    if (p instanceof String) {};\n" +
			  "  }\n" +
			  "}\n"},
		null /* no expected output string */,
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		customOptions,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test1059_options_cannot_be_null_check() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			  """
				public class X {
				  void foo(Object p) {
				    Object o = new Object();
				    if (o == null) {}
				  }
				}
				"""},
		null /* no expected output string */,
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		customOptions,
		null /* no custom requestor*/,
	  	false /* do not skip javac for this peculiar test */);
}
// encoding validation
public void test1500() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, int i, boolean b, Object u) {
				    o.toString();
				    switch (i) {
				      case 0:
				        if (b) {
				          o = u;
				        } else {
				          o = new Object();
				        }
				        break;
				    }
				    if (o == null) { /* empty */ }
				  }
				}
				"""},
		"");
}

// encoding validation
public void test1501() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, int i, boolean b, Object u) {
				    if (b) {
				      o = new Object();
				    }
				    o.toString();
				    switch (i) {
				      case 0:
				        if (b) {
				          o = u;
				        } else {
				          o = new Object();
				        }
				        break;
				    }
				    if (o == null) { /* empty */ }
				  }
				}
				"""},
		"");
}

// encoding validation
public void test1502() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, int i, boolean b, Object u) {
				    if (b) {
				      o = u;
				    }
				    o.toString();
				    switch (i) {
				      case 0:
				        if (b) {
				          o = u;
				        } else {
				          o = new Object();
				        }
				        break;
				    }
				    if (o == null) { /* empty */ }
				  }
				}
				"""},
		"");
}

// encoding validation
public void test1503() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(Object o, int i, boolean b, Object u) {
				    if (b) {
				      o = u;
				    } else {
				      o = new Object();
				    }
				    o.toString();
				    switch (i) {
				      case 0:
				        if (b) {
				          o = u;
				        } else {
				          o = new Object();
				        }
				        break;
				    }
				    if (o == null) { /* empty */ }
				  }
				}
				"""},
		"");
}

// flow info low-level validation
public void test2000_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"\n" +
			"  void foo() {\n" +
			"    Object o0 = new Object(), o1 = o0, o2 = o0, o3 = o0, o4 = o0,\n" +
			"      o5 = o0, o6 = o0, o7 = o0, o8 = o0, o9 = o0,\n" +
			"      o10 = o0, o11 = o0, o12 = o0, o13 = o0, o14 = o0,\n" +
			"      o15 = o0, o16 = o0, o17 = o0, o18 = o0, o19 = o0,\n" +
			"      o20 = o0, o21 = o0, o22 = o0, o23 = o0, o24 = o0,\n" +
			"      o25 = o0, o26 = o0, o27 = o0, o28 = o0, o29 = o0,\n" +
			"      o30 = o0, o31 = o0, o32 = o0, o33 = o0, o34 = o0,\n" +
			"      o35 = o0, o36 = o0, o37 = o0, o38 = o0, o39 = o0,\n" +
			"      o40 = o0, o41 = o0, o42 = o0, o43 = o0, o44 = o0,\n" +
			"      o45 = o0, o46 = o0, o47 = o0, o48 = o0, o49 = o0,\n" +
			"      o50 = o0, o51 = o0, o52 = o0, o53 = o0, o54 = o0,\n" +
			"      o55 = o0, o56 = o0, o57 = o0, o58 = o0, o59 = o0,\n" +
			"      o60 = o0, o61 = o0, o62 = o0, o63 = o0, o64 = o0,\n" +
			"      o65 = o0, o66 = o0, o67 = o0, o68 = o0, o69 = o0;\n" +
			"    if (o65 == null) { /* */ }\n" + // complain
			"    if (o65 != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 18)
				if (o65 == null) { /* */ }
				    ^^^
			Null comparison always yields false: The variable o65 cannot be null at this location
			----------
			2. WARNING in X.java (at line 18)
				if (o65 == null) { /* */ }
				                 ^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 19)
				if (o65 != null) { /* */ }
				    ^^^
			Redundant null check: The variable o65 cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

public void test2001_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				  void foo(
				    Object o0, Object o1, Object o2, Object o3, Object o4,
				      Object o5, Object o6, Object o7, Object o8, Object o9,
				      Object o10, Object o11, Object o12, Object o13, Object o14,
				      Object o15, Object o16, Object o17, Object o18, Object o19,
				      Object o20, Object o21, Object o22, Object o23, Object o24,
				      Object o25, Object o26, Object o27, Object o28, Object o29,
				      Object o30, Object o31, Object o32, Object o33, Object o34,
				      Object o35, Object o36, Object o37, Object o38, Object o39,
				      Object o40, Object o41, Object o42, Object o43, Object o44,
				      Object o45, Object o46, Object o47, Object o48, Object o49,
				      Object o50, Object o51, Object o52, Object o53, Object o54,
				      Object o55, Object o56, Object o57, Object o58, Object o59,
				      Object o60, Object o61, Object o62, Object o63, Object o64,
				      Object o65, Object o66, Object o67, Object o68, Object o69) {
				    if (o65 == null) { /* */ }
				    if (o65 != null) { /* */ }
				  }
				}
				"""},
		"");
}

public void test2002_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo(Object o) {
				    if (o == null) { /* */ }
				    if (o != null) { /* */ }
				  }
				}
				"""},
		"");
}

public void test2003_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo(Object o) {
				    o.toString();
				  }
				}
				"""},
		"");
}

public void test2004_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo() {
				    Object o;
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 17)
				if (o == null) { /* */ }
				    ^
			The local variable o may not have been initialized
			----------
			""");
}

public void test2005_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo(Object o) {
				    o = null;
				  }
				}
				"""},
		"");
}

public void test2006_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo() {
				    Object o = null;
				  }
				}
				"""},
		"");
}

public void test2007_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo() {
				    Object o[] = null;
				  }
				}
				"""},
		"");
}

// null analysis -- flow info
public void test2008_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  Object m0, m1, m2, m3, m4,\n" +
			"    m5, m6, m7, m8, m9,\n" +
			"    m10, m11, m12, m13, m14,\n" +
			"    m15, m16, m17, m18, m19,\n" +
			"    m20, m21, m22, m23, m24,\n" +
			"    m25, m26, m27, m28, m29,\n" +
			"    m30, m31, m32, m33, m34,\n" +
			"    m35, m36, m37, m38, m39,\n" +
			"    m40, m41, m42, m43, m44,\n" +
			"    m45, m46, m47, m48, m49,\n" +
			"    m50, m51, m52, m53, m54,\n" +
			"    m55, m56, m57, m58, m59,\n" +
			"    m60, m61, m62, m63;\n" +
			"  void foo(boolean b) {\n" +
			"    Object o = null;\n" +
			"    while (o == null) {\n" +
			     // quiet: first iteration is sure to find o null,
			     // but other iterations may change it
			"      try { /* */ }\n" +
			"      finally {\n" +
			"        if (b) {\n" +
			"          o = new Object();\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"},
		"");
}

// null analysis -- flow info
public void test2009_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m0, m1, m2, m3, m4,
				    m5, m6, m7, m8, m9,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo(Object o) {
				    try { /* */ }
				    finally {
				      o = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 20)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 20)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2010_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m00, m01, m02, m03, m04,
				    m05, m06, m07, m08, m09,
				    m10, m11, m12, m13, m14,
				    m15, m16, m17, m18, m19,
				    m20, m21, m22, m23, m24,
				    m25, m26, m27, m28, m29,
				    m30, m31, m32, m33, m34,
				    m35, m36, m37, m38, m39,
				    m40, m41, m42, m43, m44,
				    m45, m46, m47, m48, m49,
				    m50, m51, m52, m53, m54,
				    m55, m56, m57, m58, m59,
				    m60, m61, m62, m63;
				  void foo() {
				    Object o;
				    try { /* */ }
				    finally {
				      o = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 21)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 21)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2011_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
				    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
				    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
				    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
				    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
				    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
				    m060, m061, m062, m063;
				  void foo() {
				    Object o000, o001, o002, o003, o004, o005, o006, o007, o008, o009,
				      o010, o011, o012, o013, o014, o015, o016, o017, o018, o019,
				      o020, o021, o022, o023, o024, o025, o026, o027, o028, o029,
				      o030, o031, o032, o033, o034, o035, o036, o037, o038, o039,
				      o040, o041, o042, o043, o044, o045, o046, o047, o048, o049,
				      o050, o051, o052, o053, o054, o055, o056, o057, o058, o059,
				      o060, o061, o062, o063;
				    Object o;
				    try {
				      o000 = new Object();
				    }
				    finally {
				      o = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 24)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 24)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2012_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
				    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
				    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
				    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
				    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
				    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
				    m060, m061, m062, m063;
				  void foo() {
				    Object o000, o001, o002, o003, o004, o005, o006, o007, o008, o009,
				      o010, o011, o012, o013, o014, o015, o016, o017, o018, o019,
				      o020, o021, o022, o023, o024, o025, o026, o027, o028, o029,
				      o030, o031, o032, o033, o034, o035, o036, o037, o038, o039,
				      o040, o041, o042, o043, o044, o045, o046, o047, o048, o049,
				      o050, o051, o052, o053, o054, o055, o056, o057, o058, o059,
				      o060, o061, o062, o063;
				    Object o;
				    try {
				      o = new Object();
				    }
				    finally {
				      o000 = new Object();
				    }
				    if (o == null) { /* */ }
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 24)
				if (o == null) { /* */ }
				    ^
			Null comparison always yields false: The variable o cannot be null at this location
			----------
			2. WARNING in X.java (at line 24)
				if (o == null) { /* */ }
				               ^^^^^^^^^
			Dead code
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2013_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  boolean dummy;
				  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
				    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
				    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
				    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
				    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
				    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
				    m060, m061, m062, m063;
				  void foo(Object u) {
				    Object o = null;
				    while (dummy) {
				      o = u;
				    }
				    o.toString();
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 15)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// null analysis -- flow info
public void test2014_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  int m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
				    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
				    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
				    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
				    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
				    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
				    m060, m061, m062, m063;
				  final int m064;
				  X() {
				    m064 = 10;
				    class Inner extends X {
				      int m100, m101, m102, m103, m104, m105, m106, m107, m108, m109,
				        m110, m111, m112, m113, m114, m115, m116, m117, m118, m119,
				        m120, m121, m122, m123, m124, m125, m126, m127, m128, m129,
				        m130, m131, m132, m133, m134, m135, m136, m137, m138, m139,
				        m140, m141, m142, m143, m144, m145, m146, m147, m148, m149,
				        m150, m151, m152, m153, m154, m155, m156, m157, m158, m159,
				        m160, m161, m162, m163;
				      final int m164;
				      int bar() {
				        return m100 + m101 + m102 + m103 + m104 +
				               m105 + m106 + m107 + m108 + m109 +
				               m110 + m111 + m112 + m113 + m114 +
				               m115 + m116 + m117 + m118 + m119 +
				               m120 + m121 + m122 + m123 + m124 +
				               m125 + m126 + m127 + m128 + m129 +
				               m130 + m131 + m132 + m133 + m134 +
				               m135 + m136 + m137 + m138 + m139 +
				               m140 + m141 + m142 + m143 + m144 +
				               m145 + m146 + m147 + m148 + m149 +
				               m150 + m151 + m152 + m153 + m154 +
				               m155 + m156 + m157 + m158 + m159 +
				               m160 + m161 + m162 + m163 + m164;
				      }
				    };
				    System.out.println((new Inner()).bar());
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 20)
				final int m164;
				          ^^^^
			The blank final field m164 may not have been initialized
			----------
			""");
}

// null analysis -- flow info
public void test2015_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  int m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
				    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
				    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
				    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
				    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
				    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
				    m060, m061, m062, m063;
				  final int m200;
				  int m201, m202, m203, m204, m205, m206, m207, m208, m209,
				    m210, m211, m212, m213, m214, m215, m216, m217, m218, m219,
				    m220, m221, m222, m223, m224, m225, m226, m227, m228, m229,
				    m230, m231, m232, m233, m234, m235, m236, m237, m238, m239,
				    m240, m241, m242, m243, m244, m245, m246, m247, m248, m249,
				    m250, m251, m252, m253, m254, m255, m256, m257, m258, m259,
				    m260, m261, m262, m263;
				  int m301, m302, m303, m304, m305, m306, m307, m308, m309,
				    m310, m311, m312, m313, m314, m315, m316, m317, m318, m319,
				    m320, m321, m322, m323, m324, m325, m326, m327, m328, m329,
				    m330, m331, m332, m333, m334, m335, m336, m337, m338, m339,
				    m340, m341, m342, m343, m344, m345, m346, m347, m348, m349,
				    m350, m351, m352, m353, m354, m355, m356, m357, m358, m359,
				    m360, m361, m362, m363;
				  X() {
				    m200 = 10;
				    class Inner extends X {
				      int m100, m101, m102, m103, m104, m105, m106, m107, m108, m109,
				        m110, m111, m112, m113, m114, m115, m116, m117, m118, m119,
				        m120, m121, m122, m123, m124, m125, m126, m127, m128, m129,
				        m130, m131, m132, m133, m134, m135, m136, m137, m138, m139,
				        m140, m141, m142, m143, m144, m145, m146, m147, m148, m149,
				        m150, m151, m152, m153, m154, m155, m156, m157, m158, m159,
				        m160, m161, m162, m163;
				      final int m164;
				      int bar() {
				        return m100 + m101 + m102 + m103 + m104 +
				               m105 + m106 + m107 + m108 + m109 +
				               m110 + m111 + m112 + m113 + m114 +
				               m115 + m116 + m117 + m118 + m119 +
				               m120 + m121 + m122 + m123 + m124 +
				               m125 + m126 + m127 + m128 + m129 +
				               m130 + m131 + m132 + m133 + m134 +
				               m135 + m136 + m137 + m138 + m139 +
				               m140 + m141 + m142 + m143 + m144 +
				               m145 + m146 + m147 + m148 + m149 +
				               m150 + m151 + m152 + m153 + m154 +
				               m155 + m156 + m157 + m158 + m159 +
				               m160 + m161 + m162 + m163 + m164;
				      }
				    };
				    System.out.println((new Inner()).bar());
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 34)
				final int m164;
				          ^^^^
			The blank final field m164 may not have been initialized
			----------
			""");
}

// null analysis -- flow info
public void test2016_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  int m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,
				    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,
				    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,
				    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,
				    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,
				    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,
				    m060, m061;
				  final int m062;
				  {
				    int l063, m201 = 0, m202, m203, m204, m205, m206, m207, m208, m209,
				      m210, m211, m212, m213, m214, m215, m216, m217, m218, m219,
				      m220, m221, m222, m223, m224, m225, m226, m227, m228, m229,
				      m230, m231, m232, m233, m234, m235, m236, m237, m238, m239,
				      m240, m241, m242, m243, m244, m245, m246, m247, m248, m249,
				      m250, m251, m252, m253, m254, m255, m256, m257, m258, m259,
				      m260, m261, m262, m263;
				    int m301, m302, m303, m304, m305, m306, m307, m308, m309,
				      m310, m311, m312, m313, m314, m315, m316, m317, m318, m319,
				      m320, m321, m322, m323, m324, m325, m326, m327, m328, m329,
				      m330, m331, m332, m333, m334, m335, m336, m337, m338, m339,
				      m340, m341, m342, m343, m344, m345, m346, m347, m348, m349,
				      m350, m351, m352, m353, m354, m355, m356, m357, m358, m359,
				      m360 = 0, m361 = 0, m362 = 0, m363 = 0;
				    m062 = m360;
				  }
				  X() {
				    int l0, l1;
				    m000 = l1;
				    class Inner extends X {
				      int bar() {
				        return 0;
				      }
				    };
				    System.out.println((new Inner()).bar());
				  }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 29)
				m000 = l1;
				       ^^
			The local variable l1 may not have been initialized
			----------
			""");
}

public void test2017_flow_info() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo(Object u) {\n" +
			"    Object o = null;\n" +
			"    while (dummy) {\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        o = u;\n" +
			"        continue;\n" +
			"      }\n" +
			"    }\n" +
			"    if (o != null) { /* */ }\n" +
			"  }\n" +
			"}\n"},
		"");
}

public void test2018_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    while (dummy) {\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        o = null;\n" +
			"        continue;\n" +
			"      }\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 18)
				o.toString();
				^
			The local variable o may not have been initialized
			----------
			2. ERROR in X.java (at line 18)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""");
}

public void test2019_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  void foo() {\n" +
			"    Object o;\n" +
			"    while (dummy) {\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        continue;\n" +
			"      }\n" +
			"      o = null;\n" +
			"    }\n" +
			"    o.toString();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 18)
				o.toString();
				^
			The local variable o may not have been initialized
			----------
			2. ERROR in X.java (at line 18)
				o.toString();
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""");
}

public void test2020_flow_info() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  boolean dummy;\n" +
			"  Object m000, m001, m002, m003, m004, m005, m006, m007, m008, m009,\n" +
			"    m010, m011, m012, m013, m014, m015, m016, m017, m018, m019,\n" +
			"    m020, m021, m022, m023, m024, m025, m026, m027, m028, m029,\n" +
			"    m030, m031, m032, m033, m034, m035, m036, m037, m038, m039,\n" +
			"    m040, m041, m042, m043, m044, m045, m046, m047, m048, m049,\n" +
			"    m050, m051, m052, m053, m054, m055, m056, m057, m058, m059,\n" +
			"    m060, m061, m062, m063;\n" +
			"  int m200, m201, m202, m203, m204, m205, m206, m207, m208, m209,\n" +
			"    m210, m211, m212, m213, m214, m215, m216, m217, m218, m219,\n" +
			"    m220, m221, m222, m223, m224, m225, m226, m227, m228, m229,\n" +
			"    m230, m231, m232, m233, m234, m235, m236, m237, m238, m239,\n" +
			"    m240, m241, m242, m243, m244, m245, m246, m247, m248, m249,\n" +
			"    m250, m251, m252, m253, m254, m255, m256, m257, m258, m259,\n" +
			"    m260, m261;\n" +
			"  void foo() {\n" +
			"    Object o0, o1;\n" +
			"    while (dummy) {\n" +
			"      o0 = new Object();\n" +
			"      if (dummy) {\n" + // uncorrelated
			"        o1 = null;\n" +
			"        continue;\n" +
			"      }\n" +
			"    }\n" +
			"    o1.toString();\n" +
			"  }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 26)
				o1.toString();
				^^
			The local variable o1 may not have been initialized
			----------
			2. ERROR in X.java (at line 26)
				o1.toString();
				^^
			Potential null pointer access: The variable o1 may be null at this location
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=291418
// Test to verify that redundant null checks are properly reported in all loops
public void testBug291418a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
				new String[] {
						"X.java",
						"class X {\n" +
						"  void foo(int[] argArray) {\n" +
						"    int[] array = {2};\n" +
						"    int[] collectionVar = {1,2};\n" +
						"	 if(argArray == null) return;\n" +
						"    for(int x:collectionVar) {\n" +
						"        if (collectionVar == null);\n" +	// collectionVar cannot be null here
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    }\n" +
						"	 int count = 0;\n" +
						"    do {\n" +
						"		 count++;\n" +
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    } while (count<10);\n" +
						"	 array = new int[0];\n" + 			// reset tainting by null check
						"	 if (argArray == null) return;\n" + // reset tainting by null check
						"    for (int i=0; i<2; i++) {\n" +
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    }\n" +
						"    while (true) {\n" +
						"        if (array == null);\n" +				// array is not null here
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"    }\n" +
						"  }\n" +
						"}"},
				"""
					----------
					1. ERROR in X.java (at line 7)
						if (collectionVar == null);
						    ^^^^^^^^^^^^^
					Null comparison always yields false: The variable collectionVar cannot be null at this location
					----------
					2. ERROR in X.java (at line 8)
						if (array == null);
						    ^^^^^
					Null comparison always yields false: The variable array cannot be null at this location
					----------
					3. ERROR in X.java (at line 9)
						if (argArray == null);
						    ^^^^^^^^
					Null comparison always yields false: The variable argArray cannot be null at this location
					----------
					4. ERROR in X.java (at line 14)
						if (array == null);
						    ^^^^^
					Null comparison always yields false: The variable array cannot be null at this location
					----------
					5. ERROR in X.java (at line 15)
						if (argArray == null);
						    ^^^^^^^^
					Null comparison always yields false: The variable argArray cannot be null at this location
					----------
					6. ERROR in X.java (at line 20)
						if (array == null);
						    ^^^^^
					Null comparison always yields false: The variable array cannot be null at this location
					----------
					7. ERROR in X.java (at line 21)
						if (argArray == null);
						    ^^^^^^^^
					Null comparison always yields false: The variable argArray cannot be null at this location
					----------
					8. ERROR in X.java (at line 24)
						if (array == null);
						    ^^^^^
					Null comparison always yields false: The variable array cannot be null at this location
					----------
					9. ERROR in X.java (at line 25)
						if (argArray == null);
						    ^^^^^^^^
					Null comparison always yields false: The variable argArray cannot be null at this location
					----------
					""");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=291418
// Test to verify that redundant null checks are properly reported
// in a loop in case the null status is modified downstream in the loop
public void testBug291418b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
				new String[] {
						"X.java",
						"class X {\n" +
						"  void foo(int[] argArray) {\n" +
						"    int[] array = {2};\n" +
						"    int[] collectionVar = {1,2};\n" +
						"	 if(argArray == null) return;" +
						"    for(int x:collectionVar) {\n" +
						"        if (collectionVar == null);\n" +	// collectionVar cannot be null here
						"        if (array == null);\n" +		// array is not null in first iteration but assigned null later in the loop. So we keep quiet
						"		 if (argArray == null);\n" +		// argArray cannot be null here
						"		 array = null;\n" +
						"    }\n" +
						"  }\n" +
						"}"},
				"""
					----------
					1. ERROR in X.java (at line 6)
						if (collectionVar == null);
						    ^^^^^^^^^^^^^
					Null comparison always yields false: The variable collectionVar cannot be null at this location
					----------
					2. ERROR in X.java (at line 8)
						if (argArray == null);
						    ^^^^^^^^
					Null comparison always yields false: The variable argArray cannot be null at this location
					----------
					""");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293917
// Test that a redundant null check doesn't affect the null status of
// a variable downstream.
public void testBug293917a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(){\n" +
			"		String x = null, y = null;\n" +
			"		if (x == null) x = \"foo\";\n" +
			"		if (x != null) y = \"bar\";\n" +
			"		x.length();\n" +   // shouldn't warn here
			"		y.length();\n" +   // shouldn't warn here
			"	}\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (x == null) x = "foo";
				    ^
			Redundant null check: The variable x can only be null at this location
			----------
			2. ERROR in X.java (at line 5)
				if (x != null) y = "bar";
				    ^
			Redundant null check: The variable x cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293917
// Test that a redundant null check doesn't affect the null status of
// a variable downstream in a loop.
public void testBug293917b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(){\n" +
			"		String x = null, y = null;" +
			"		while(true) {\n" +
			"			if (x == null) x = \"foo\";\n" +
			"			if (x != null) y = \"bar\";\n" +
			"			x.length();\n" +   // shouldn't warn here
			"			y.length();\n" +   // shouldn't warn here
			"		}\n" +
			"	}\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (x != null) y = "bar";
				    ^
			Redundant null check: The variable x cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=293917
// Test that a redundant null check doesn't affect the null status of
// a variable downstream in a finally block.
public void testBug293917c() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void foo(){\n" +
			"		String x = null, y = null;" +
			"		try {}\n" +
			"		finally {\n" +
			"			if (x == null) x = \"foo\";\n" +
			"			if (x != null) y = \"bar\";\n" +
			"			x.length();\n" +   // shouldn't warn here
			"			y.length();\n" +   // shouldn't warn here
			"		}\n" +
			"	}\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (x == null) x = "foo";
				    ^
			Redundant null check: The variable x can only be null at this location
			----------
			2. ERROR in X.java (at line 6)
				if (x != null) y = "bar";
				    ^
			Redundant null check: The variable x cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=190623
// Test that a redundant null check doesn't affect the null status of
// a variable downstream.
public void testBug190623() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        Number n = getNumber();
				        if (n instanceof Double) {
				            Double d= (Double) n;
				            if (d != null && d.isNaN()) {
				                System.out.println("outside loop");
				            }
				            for (int i= 0; i < 10; i++) {
				                if (d != null && d.isNaN()) {
				                    System.out.println("inside loop");
				                }
				            }
				        }
				    }
				    private static Number getNumber() {
				        return Double.valueOf(Math.sqrt(-1));
				    }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (d != null && d.isNaN()) {
				    ^
			Redundant null check: The variable d cannot be null at this location
			----------
			2. ERROR in X.java (at line 10)
				if (d != null && d.isNaN()) {
				    ^
			Redundant null check: The variable d cannot be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=299900
//Test to verify that null checks are properly reported for the variable(s)
//in the right expression of an OR condition statement.
public void testBug299900a() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(Object foo, Object bar) {
				    if(foo == null || bar == null) {
					 	System.out.println(foo.toString());
					 	System.out.println(bar.toString());
				    }
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 4)
				System.out.println(foo.toString());
				                   ^^^
			Potential null pointer access: The variable foo may be null at this location
			----------
			2. ERROR in X.java (at line 5)
				System.out.println(bar.toString());
				                   ^^^
			Potential null pointer access: The variable bar may be null at this location
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=299900
//Test to verify that null checks are properly reported for the variable(s)
//in the right expression of an OR condition statement.
public void testBug299900b() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo(Object foo, Object bar) {
				    if(foo == null || bar == null) {
				    }
					 System.out.println(foo.toString());
					 System.out.println(bar.toString());
				  }
				}"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				System.out.println(foo.toString());
				                   ^^^
			Potential null pointer access: The variable foo may be null at this location
			----------
			2. ERROR in X.java (at line 6)
				System.out.println(bar.toString());
				                   ^^^
			Potential null pointer access: The variable bar may be null at this location
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// Test whether Null pointer access warnings are being reported correctly when auto-unboxing
public void testBug253896a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public void foo() {
					    Integer f1 = null;
						 if(f1 == 1)
					 	 	System.out.println("f1 is 1");
					    Integer f2 = null;
						 int abc = (f2 != 1)? 1 : 0;
					    Float f3 = null;
						 if(f3 == null)
					 	 	System.out.println("f3 is null");
					    Byte f4 = null;
						 if(f4 != null)
					 	 	System.out.println("f4 is not null");
					  }
					}"""},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if(f1 == 1)
					   ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				2. ERROR in X.java (at line 7)
					int abc = (f2 != 1)? 1 : 0;
					           ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				3. ERROR in X.java (at line 9)
					if(f3 == null)
					   ^^
				Redundant null check: The variable f3 can only be null at this location
				----------
				4. ERROR in X.java (at line 12)
					if(f4 != null)
					   ^^
				Null comparison always yields false: The variable f4 can only be null at this location
				----------
				5. WARNING in X.java (at line 13)
					System.out.println("f4 is not null");
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				""");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// To test whether null pointer access and potential null pointer access warnings are correctly reported when auto-unboxing
public void testBug253896b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo(Integer i1, Integer i2) {\n" +
				"	 if(i1 == null && i2 == null){\n" +
				"		if(i1 == 1)\n" +
				" 	 	System.out.println(i1);}\n" +	//i1 is definitely null here
				"	 else {\n" +
				"		if(i1 == 0) {}\n" +		//i1 may be null here.
				"	 }\n" +
				"  }\n" +
				"}"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					if(i1 == 1)
					   ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				2. ERROR in X.java (at line 7)
					if(i1 == 0) {}
					   ^^
				Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
				----------
				""");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// Test whether Null pointer access warnings are being reported correctly when auto-unboxing inside loops
public void testBug253896c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"	 Integer a = null;\n" +
				"	 Integer outer2 = null;\n" +
				"	 while (true) {\n" +
				"    	Integer f1 = null;\n" +
				"	 	if(f1 == 1)\n" +
				" 	 		System.out.println(\"f1 is 1\");\n" +
				"    	Integer f2 = null;\n" +
				"	 	int abc = (f2 != 1)? 1 : 0;\n" +
				"    	Float f3 = null;\n" +
				"	 	if(f3 == null)\n" +
				" 	 		System.out.println(\"f3 is null\");\n" +
				"    	Byte f4 = null;\n" +
				"	 	if(f4 != null)\n" +
				" 	 		System.out.println(\"f4 is not null\");\n" +
				"		if(a == 1) {}\n" +	// warn null reference in deferred check case
				"		if(outer2 == 1) {}\n" +	// warn potential null reference in deferred check case
				"		outer2 = 1;\n" +
				"	 }\n" +
				"  }\n" +
				"}"},
			"""
				----------
				1. ERROR in X.java (at line 7)
					if(f1 == 1)
					   ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				2. ERROR in X.java (at line 10)
					int abc = (f2 != 1)? 1 : 0;
					           ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				3. ERROR in X.java (at line 12)
					if(f3 == null)
					   ^^
				Redundant null check: The variable f3 can only be null at this location
				----------
				4. ERROR in X.java (at line 15)
					if(f4 != null)
					   ^^
				Null comparison always yields false: The variable f4 can only be null at this location
				----------
				5. WARNING in X.java (at line 16)
					System.out.println("f4 is not null");
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				6. ERROR in X.java (at line 17)
					if(a == 1) {}
					   ^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				7. ERROR in X.java (at line 18)
					if(outer2 == 1) {}
					   ^^^^^^
				Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
				----------
				""");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253896
// Test whether Null pointer access warnings are being reported correctly when auto-unboxing inside finally contexts
public void testBug253896d() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo(Integer param) {\n" +
				"	 Integer outer = null;\n" +
				"	 if (param == null) {}\n" +	//tainting param
				"	 try {}\n" +
				"	 finally {\n" +
				"    	Integer f1 = null;\n" +
				"	 	if(f1 == 1)\n" +
				" 	 		System.out.println(\"f1 is 1\");\n" +
				"    	Integer f2 = null;\n" +
				"	 	int abc = (f2 != 1)? 1 : 0;\n" +
				"    	Float f3 = null;\n" +
				"	 	if(f3 == null)\n" +
				" 	 		System.out.println(\"f3 is null\");\n" +
				"    	Byte f4 = null;\n" +
				"	 	if(f4 != null)\n" +
				" 	 		System.out.println(\"f4 is not null\");\n" +
				"		if(outer == 1) {}\n" +  // warn null reference in deferred check case
				"		if(param == 1) {}\n" +
				"	 }\n" +
				"  }\n" +
				"}"},
			"""
				----------
				1. ERROR in X.java (at line 8)
					if(f1 == 1)
					   ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				2. ERROR in X.java (at line 11)
					int abc = (f2 != 1)? 1 : 0;
					           ^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				3. ERROR in X.java (at line 13)
					if(f3 == null)
					   ^^
				Redundant null check: The variable f3 can only be null at this location
				----------
				4. ERROR in X.java (at line 16)
					if(f4 != null)
					   ^^
				Null comparison always yields false: The variable f4 can only be null at this location
				----------
				5. WARNING in X.java (at line 17)
					System.out.println("f4 is not null");
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				6. ERROR in X.java (at line 18)
					if(outer == 1) {}
					   ^^^^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				7. ERROR in X.java (at line 19)
					if(param == 1) {}
					   ^^^^^
				Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
				----------
				""");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303448
//To check that code gen is not optimized for an if statement
//where a local variable's definite nullness or otherwise is known because of
//an earlier assert expression (inside finally context)
public void testBug303448a() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo() {
							Object foo = null;
							Object foo2 = null;
							try {}\s
							finally {
							assert (foo != null && foo2 != null);
							if (foo != null) {
								System.out.println("foo is not null");
							} else {
								System.out.println("foo is null");
							}
							if (foo2 != null) {
								System.out.println("foo2 is not null");
							} else {
								System.out.println("foo2 is null");
							}
							}
						}
					}
					""",
			},
			"",
			null,
			true,
			null,
			options,
			null); // custom requestor

		String expectedOutput = this.complianceLevel < ClassFileConstants.JDK1_5?
				"""
					  // Method descriptor #11 ()V
					  // Stack: 2, Locals: 3
					  public void foo();
					     0  aconst_null
					     1  astore_1 [foo]
					     2  aconst_null
					     3  astore_2 [foo2]
					     4  getstatic X.$assertionsDisabled : boolean [38]
					     7  ifne 26
					    10  aload_1 [foo]
					    11  ifnull 18
					    14  aload_2 [foo2]
					    15  ifnonnull 26
					    18  new java.lang.AssertionError [49]
					    21  dup
					    22  invokespecial java.lang.AssertionError() [51]
					    25  athrow
					    26  aload_1 [foo]
					    27  ifnull 41
					    30  getstatic java.lang.System.out : java.io.PrintStream [52]
					    33  ldc <String "foo is not null"> [58]
					    35  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]
					    38  goto 49
					    41  getstatic java.lang.System.out : java.io.PrintStream [52]
					    44  ldc <String "foo is null"> [65]
					    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]
					    49  aload_2 [foo2]
					    50  ifnull 64
					    53  getstatic java.lang.System.out : java.io.PrintStream [52]
					    56  ldc <String "foo2 is not null"> [67]
					    58  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]
					    61  goto 72
					    64  getstatic java.lang.System.out : java.io.PrintStream [52]
					    67  ldc <String "foo2 is null"> [69]
					    69  invokevirtual java.io.PrintStream.println(java.lang.String) : void [60]
					    72  return
					      Line numbers:
					        [pc: 0, line: 3]
					        [pc: 2, line: 4]
					        [pc: 4, line: 7]
					        [pc: 26, line: 8]
					        [pc: 30, line: 9]
					        [pc: 38, line: 10]
					        [pc: 41, line: 11]
					        [pc: 49, line: 13]
					        [pc: 53, line: 14]
					        [pc: 61, line: 15]
					        [pc: 64, line: 16]
					        [pc: 72, line: 19]
					      Local variable table:
					        [pc: 0, pc: 73] local: this index: 0 type: X
					        [pc: 2, pc: 73] local: foo index: 1 type: java.lang.Object
					        [pc: 4, pc: 73] local: foo2 index: 2 type: java.lang.Object
					"""
			: 	this.complianceLevel < ClassFileConstants.JDK1_6?
						"""
							  // Method descriptor #8 ()V
							  // Stack: 2, Locals: 3
							  public void foo();
							     0  aconst_null
							     1  astore_1 [foo]
							     2  aconst_null
							     3  astore_2 [foo2]
							     4  getstatic X.$assertionsDisabled : boolean [16]
							     7  ifne 26
							    10  aload_1 [foo]
							    11  ifnull 18
							    14  aload_2 [foo2]
							    15  ifnonnull 26
							    18  new java.lang.AssertionError [26]
							    21  dup
							    22  invokespecial java.lang.AssertionError() [28]
							    25  athrow
							    26  aload_1 [foo]
							    27  ifnull 41
							    30  getstatic java.lang.System.out : java.io.PrintStream [29]
							    33  ldc <String "foo is not null"> [35]
							    35  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]
							    38  goto 49
							    41  getstatic java.lang.System.out : java.io.PrintStream [29]
							    44  ldc <String "foo is null"> [43]
							    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]
							    49  aload_2 [foo2]
							    50  ifnull 64
							    53  getstatic java.lang.System.out : java.io.PrintStream [29]
							    56  ldc <String "foo2 is not null"> [45]
							    58  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]
							    61  goto 72
							    64  getstatic java.lang.System.out : java.io.PrintStream [29]
							    67  ldc <String "foo2 is null"> [47]
							    69  invokevirtual java.io.PrintStream.println(java.lang.String) : void [37]
							    72  return
							      Line numbers:
							        [pc: 0, line: 3]
							        [pc: 2, line: 4]
							        [pc: 4, line: 7]
							        [pc: 26, line: 8]
							        [pc: 30, line: 9]
							        [pc: 38, line: 10]
							        [pc: 41, line: 11]
							        [pc: 49, line: 13]
							        [pc: 53, line: 14]
							        [pc: 61, line: 15]
							        [pc: 64, line: 16]
							        [pc: 72, line: 19]
							      Local variable table:
							        [pc: 0, pc: 73] local: this index: 0 type: X
							        [pc: 2, pc: 73] local: foo index: 1 type: java.lang.Object
							        [pc: 4, pc: 73] local: foo2 index: 2 type: java.lang.Object
							"""
					:	"""
						  // Method descriptor #8 ()V
						  // Stack: 2, Locals: 3
						  public void foo();
						     0  aconst_null
						     1  astore_1 [foo]
						     2  aconst_null
						     3  astore_2 [foo2]
						     4  getstatic X.$assertionsDisabled : boolean [16]
						     7  ifne 26
						    10  aload_1 [foo]
						    11  ifnull 18
						    14  aload_2 [foo2]
						    15  ifnonnull 26
						    18  new java.lang.AssertionError [27]
						    21  dup
						    22  invokespecial java.lang.AssertionError() [29]
						    25  athrow
						    26  aload_1 [foo]
						    27  ifnull 41
						    30  getstatic java.lang.System.out : java.io.PrintStream [30]
						    33  ldc <String "foo is not null"> [36]
						    35  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]
						    38  goto 49
						    41  getstatic java.lang.System.out : java.io.PrintStream [30]
						    44  ldc <String "foo is null"> [44]
						    46  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]
						    49  aload_2 [foo2]
						    50  ifnull 64
						    53  getstatic java.lang.System.out : java.io.PrintStream [30]
						    56  ldc <String "foo2 is not null"> [46]
						    58  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]
						    61  goto 72
						    64  getstatic java.lang.System.out : java.io.PrintStream [30]
						    67  ldc <String "foo2 is null"> [48]
						    69  invokevirtual java.io.PrintStream.println(java.lang.String) : void [38]
						    72  return
						      Line numbers:
						        [pc: 0, line: 3]
						        [pc: 2, line: 4]
						        [pc: 4, line: 7]
						        [pc: 26, line: 8]
						        [pc: 30, line: 9]
						        [pc: 38, line: 10]
						        [pc: 41, line: 11]
						        [pc: 49, line: 13]
						        [pc: 53, line: 14]
						        [pc: 61, line: 15]
						        [pc: 64, line: 16]
						        [pc: 72, line: 19]
						      Local variable table:
						        [pc: 0, pc: 73] local: this index: 0 type: X
						        [pc: 2, pc: 73] local: foo index: 1 type: java.lang.Object
						        [pc: 4, pc: 73] local: foo2 index: 2 type: java.lang.Object
						      Stack map table: number of frames 6
						        [pc: 18, append: {java.lang.Object, java.lang.Object}]
						        [pc: 26, same]
						        [pc: 41, same]
						        [pc: 49, same]
						        [pc: 64, same]
						        [pc: 72, same]
						""";

		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303448
//To check that code gen is not optimized for an if statement
//where a local variable's definite nullness or otherwise is known because of
//an earlier assert expression (inside finally context)
public void testBug303448b() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	if (this.complianceLevel >= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							System.out.print("start");
							Object foo = null;
							assert (foo != null);
							if (foo != null) {
								System.out.println("foo is not null");
							}
							System.out.print("end");
						}
					}
					""",
			},
			"startend",
			null,
			true,
			null,
			options,
			null);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=304416
public void testBug304416() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = null;
						String s2 = null;
						if (s != null && s2 != null) {
							System.out.println(s);
							System.out.println(s2);
						}
					}
				}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"""
		  public static void main(java.lang.String[] args);
		     0  aconst_null
		     1  astore_1 [s]
		     2  aconst_null
		     3  astore_2 [s2]
		     4  aload_1 [s]
		     5  ifnull 26
		     8  aload_2 [s2]
		     9  ifnull 26
		    12  getstatic java.lang.System.out : java.io.PrintStream [16]
		    15  aload_1 [s]
		    16  invokevirtual java.io.PrintStream.println(java.lang.String) : void [22]
		    19  getstatic java.lang.System.out : java.io.PrintStream [16]
		    22  aload_2 [s2]
		    23  invokevirtual java.io.PrintStream.println(java.lang.String) : void [22]
		    26  return
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305590
// To verify that a "instanceof always yields false" warning is not elicited in the
// case when the expression has been assigned a non null value in the instanceof check.
public void testBug305590() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  public void foo() {\n" +
			"	 Object str = null;\n" +
			"	 if ((str = \"str\") instanceof String) {}\n" + // shouldn't warn
			"	 str = null;\n" +
			"	 if ((str = \"str\") instanceof Number) {}\n" + // shouldn't warn
			"	 str = null;\n" +
			"	 if (str instanceof String) {}\n" + // should warn
			"  }\n" +
			"}"},
		"""
			----------
			1. ERROR in X.java (at line 8)
				if (str instanceof String) {}
				    ^^^
			instanceof always yields false: The variable str can only be null at this location
			----------
			""",
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
//   LocalDeclaration
public void testBug319201() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"	 Integer i = null;\n" +
				"	 int j = i;\n" + // should warn
				"  }\n" +
				"}"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					int j = i;
					        ^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing could raise an NPE
//   Assignment
public void testBug319201a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo(Integer i) {\n" +
				"    if (i == null) {};\n" +
				"	 int j;\n" +
				"	 j = i;\n" + // should warn
				"  }\n" +
				"}"},
			"""
				----------
				1. ERROR in X.java (at line 5)
					j = i;
					    ^
				Potential null pointer access: This expression of type Integer may be null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
//   MessageSend
public void testBug319201b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public void foo() {\n" +
				"    Boolean bo = null;;\n" +
				"	 bar(bo);\n" + // should warn
				"  }\n" +
				"  void bar(boolean b) {}\n" +
				"}"},
			"""
				----------
				1. ERROR in X.java (at line 4)
					bar(bo);
					    ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
// Node types covered (in this order):
//   ExplicitConstructorCall
//   AllocationExpression
//   AND_AND_Expression
//   OR_OR_Expression
//   ArrayAllocationExpression
//   ForStatement
//   DoStatement
//   IfStatement
//   QualifiedAllocationExpression
//   SwitchStatement
//   WhileStatement
//   CastExpression
//   AssertStatement
//   ReturnStatement
public void testBug319201c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	runNegativeTest(
			new String[] {
              "X.java",
              """
				class Y { public Y(boolean b1, boolean b2) {} }
				public class X extends Y {
				  public X(boolean b, Boolean b2) {
				      super(b2 == null, b2);
				  }
				  class Z {
				      public Z(boolean b) {}
				  }
				  boolean fB = (Boolean)null;
				  public boolean foo(boolean inB) {
				      Boolean b1 = null;
				      X x = new X(b1, null);
				      Boolean b2 = null;
				      boolean dontcare = b2 && inB;
				      Boolean b3 = null;
				      dontcare = inB || b3;
				      Integer dims = null;
				      char[] cs = new char[dims];
				      Boolean b5 = null;
				      do {
				          Boolean b4 = null;
				          for (int i=0;b4; i++);
				      } while (b5);
				      Boolean b6 = null;
				      if (b6) { }
				      Boolean b7 = null;
				      Z z = this.new Z(b7);
				      Integer sel = null;
				      switch(sel) {
				          case 1: break;
				          default: break;
				      }
				      Boolean b8 = null;
				      while (b8) {}
				      Boolean b9 = null;
				      dontcare = (boolean)b9;
				      Boolean b10 = null;
				      assert b10 : "shouldn't happen, but will";
				      Boolean b11 = null;
				      return b11;
				  }
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 4)
					super(b2 == null, b2);
					                  ^^
				Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
				----------
				2. ERROR in X.java (at line 9)
					boolean fB = (Boolean)null;
					             ^^^^^^^^^^^^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				3. ERROR in X.java (at line 12)
					X x = new X(b1, null);
					            ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				4. ERROR in X.java (at line 14)
					boolean dontcare = b2 && inB;
					                   ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				5. ERROR in X.java (at line 16)
					dontcare = inB || b3;
					                  ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				6. ERROR in X.java (at line 18)
					char[] cs = new char[dims];
					                     ^^^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				7. ERROR in X.java (at line 22)
					for (int i=0;b4; i++);
					             ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				8. ERROR in X.java (at line 23)
					} while (b5);
					         ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				9. ERROR in X.java (at line 25)
					if (b6) { }
					    ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				10. ERROR in X.java (at line 27)
					Z z = this.new Z(b7);
					                 ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				11. ERROR in X.java (at line 29)
					switch(sel) {
					       ^^^
				Null pointer access: This expression of type Integer is null but requires auto-unboxing
				----------
				12. ERROR in X.java (at line 34)
					while (b8) {}
					       ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				13. ERROR in X.java (at line 36)
					dontcare = (boolean)b9;
					                    ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				14. ERROR in X.java (at line 38)
					assert b10 : "shouldn't happen, but will";
					       ^^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				15. ERROR in X.java (at line 40)
					return b11;
					       ^^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				""",
		    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319201
// unboxing raises an NPE
// DoStatement, variants with assignement and/or continue in the body & empty body
public void testBug319201d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
	runNegativeTest(
			new String[] {
              "X.java",
              "public class X {\n" +
              "  public void foo(boolean cond, boolean cond2) {\n" +
              "      Boolean b = null;\n" +
              "      do {\n" +
              "          b = false;\n" +
              "          if (cond) continue;\n" +   // shouldn't make a difference
              "      } while (b);\n" + // don't complain, loop body has already assigned b
              "      Boolean b2 = null;\n" +
              "      do {\n" +
              "          if (cond) continue;\n" +
              "          b2 = false;\n" +
              "      } while (b2);\n" + // complain here: potentially null
              "      Boolean b3 = null;\n" +
              "      do {\n" +
              "      } while (b3);\n" + // complain here: definitely null
              "      Boolean b4 = null;\n" +
              "      do {\n" +
              "        if (cond) {\n" +
              "            b4 = true;\n" +
              "            if (cond2) continue;\n" +
              "        }\n" +
              "        b4 = false;\n" +
              "      } while (b4);\n" + // don't complain here: definitely non-null
              "      Boolean b5 = null;\n" +
              "      do {\n" +
              "         b5 = true;\n" +
              "      } while (b5);\n" +  // don't complain
              "      Boolean b6 = null;\n" +
              "      do {\n" +
              "         b6 = true;\n" +
              "         continue;\n" +
              "      } while (b6); \n" + // don't complain
              "      Boolean b7 = null;\n" +
              "      Boolean b8 = null;\n" +
              "      do {\n" +
              "        if (cond) {\n" +
              "            b7 = true;\n" +
              "            continue;\n" +
              "        } else {\n" +
              "            b8 = true;\n" +
              "        }\n" +
              "      } while (b7);\n" + // complain here: after else branch b7 can still be null
              "  }\n" +
			  "}"},
			"""
				----------
				1. ERROR in X.java (at line 12)
					} while (b2);
					         ^^
				Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
				----------
				2. ERROR in X.java (at line 15)
					} while (b3);
					         ^^
				Null pointer access: This expression of type Boolean is null but requires auto-unboxing
				----------
				3. ERROR in X.java (at line 42)
					} while (b7);
					         ^^
				Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
				----------
				""",
			null/*classLibraries*/,
			true/*shouldFlushOutputDirectory*/,
			customOptions,
			"",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=320414
public void testBug320414() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static class B {
						public static final int CONST = 16;
						int i;
					}
					B b;
					public static void main(String[] args) {
						new X().foo();
					}
					void foo() {
						B localB = b;\s
						int i = localB.CONST;
						if (localB != null) {
							i = localB.i;
						}
						System.out.println(i);
					}
				}""",
		},
		"16",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"""
		  void foo();
		     0  aload_0 [this]
		     1  getfield X.b : X.B [24]
		     4  astore_1 [localB]
		     5  bipush 16
		     7  istore_2 [i]
		     8  aload_1 [localB]
		     9  ifnull 17
		    12  aload_1 [localB]
		    13  getfield X$B.i : int [26]
		    16  istore_2 [i]
		    17  getstatic java.lang.System.out : java.io.PrintStream [32]
		    20  iload_2 [i]
		    21  invokevirtual java.io.PrintStream.println(int) : void [38]
		    24  return
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321926
// To verify that a "redundant null check" warning is NOT elicited for a variable assigned non-null
// in an infinite while loop inside a try catch block and that code generation shows no surprises.
public void testBug321926a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321926
// need more precise info from the throw location
public void testBug321926a2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses @SW annotation
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"	@SuppressWarnings(\"null\")\n" + // expecting "redundant null check" at "if (someVariable == null)"
			"  public static void main(String[] args) {\n" +
			"	 String someVariable = null;\n" +
			"	 int i = 0;\n" +
			"	 try {\n" +
			"		while (true) {\n" +
			"			if (i == 0){\n" +
			"				someVariable = \"not null\";\n" +
			"				i++;\n" +
			"			}\n" +
			"			else {\n" +
			"				someVariable = \"value\";\n" +
			"				throw new IOException();\n" +
			"			}\n" +
			"		}\n" +
			"	 } catch (IOException e) {\n" +
			"		// broken from loop, continue on\n" +
			"	 }\n" +
			"	 if (someVariable == null) {\n" +
			"    	System.out.println(\"Compiler buggy\");\n" +
			"	 } else {\n" +
			"		System.out.println(\"Compiler good\");\n" +
			"	 }\n" +
			"  }\n" +
			"}"},
		"Compiler good",
		options);
}
// Test that dead code warning does show up.
public void testBug321926b() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
				       System.out.println("This is dead code");
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
			"""
				----------
				1. ERROR in X.java (at line 15)
					System.out.println("This is dead code");
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Unreachable code
				----------
				""");
}
// Check nullness in catch block, finally block and downstream code.
public void testBug321926c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler buggy");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 } finally {
					 	if (someVariable == null) {
				    		System.out.println("Compiler buggy");
					 	} else {
							System.out.print("Compiler good ");
					 	}
				    }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good Compiler good Compiler good");
}
// Various nested loops.
public void testBug321926d() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       while (true) {
				           for(;;) {\s
								while (true) {
									if (i == 0){
										someVariable = "not null";
										i++;
									}
									else
										throw new IOException();
								}
							}
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler buggy");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 } finally {
					 	if (someVariable == null) {
				    		System.out.println("Compiler buggy");
					 	} else {
							System.out.print("Compiler good ");
					 	}
				    }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good Compiler good Compiler good");
}
// Test widening catch.
public void testBug321926e() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (Exception e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good");
}
// Tested nested try blocks.
public void testBug321926f() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				    public static void main(String[] args) {
				        String someVariable = null;
				        int i = 0;
				        try {
				        	while (true) {
				        		if (i != 0) {
				        			try {
				        				throw new IOException();
				        			} catch (IOException e) {
				        				if (someVariable == null) {
				        					System.out.println("The compiler is buggy");
				        				} else {
				        					System.out.print("Compiler good ");
				        				}
				        				throw e;
				        			}
				        		} else {
				        			someVariable = "not null";
				        			i++;
				        		}
				        	}
				        } catch (Exception e) {
				            // having broken from loop, continue on
				        }
				        if (someVariable == null) {
				            System.out.println("The compiler is buggy");
				        } else {
				            System.out.println("Compiler good");
				        }
				    }
				}
				"""},
		"Compiler good Compiler good");
}
// test for loop
public void testBug321926g() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						for (int j = 0; true; j++) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good");
}
// test do while loop
public void testBug321926h() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						do {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						} while(true);
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good");
}
// test with while (true) with a break inside. was working already.
public void testBug321926i() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
				               break;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good");
}
// Test with non-explicit throws, i.e call method which throws rather than an inline throw statement.
public void testBug321926j() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								invokeSomeMethod();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				  public static void invokeSomeMethod() throws IOException {
				      throw new IOException();
				  }
				}"""},
		"Compiler good");
}
// Variation with nested loops
public void testBug321926k() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       while (true) {
				       	try {
								while (true) {
									if (i == 0){
										someVariable = "not null";
										i++;
									}
									else
										throw new IOException();
								}
				       	} catch (IOException e) {
				           }
					 		if (someVariable == null) {
				    			System.out.println("Compiler buggy");
					 		} else {
								System.out.print("Compiler good ");
					 		}
				           throw new IOException();
				       }
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good Compiler good");
}
// variation with nested loops.
public void testBug321926l() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);

	this.runTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       while (true) {
				           someVariable = null;
				       	try {
								while (true) {
									if (i == 0){
										someVariable = "not null";
										i++;
									}
									else
										throw new IOException();
								}
				       	} catch (IOException e) {
				           }
					 		if (someVariable == null) {
				    			System.out.println("Compiler buggy");
					 		} else {
								System.out.print("Compiler good ");
					 		}
				           throw new IOException();
				       }
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		 false,
		 """
			----------
			1. WARNING in X.java (at line 8)
				someVariable = null;
				^^^^^^^^^^^^
			Redundant assignment: The variable someVariable can only be null at this location
			----------
			""",
		"Compiler good Compiler good",
		"",
		true, // force execution
		null, // classlibs
		true, // flush output,
		null, // vm args
		options,
		null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
public void testBug321926m() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
				           if (true) {
				               break;
				           }
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good");
}
public void testBug321926n() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						while (true) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good",
		options);
}
public void testBug321926o() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						for(;;) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good",
		options);
}
public void testBug321926p() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						do {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						} while (true);
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good",
		options);
}
public void testBug321926q() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						do {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						} while ((someVariable = "not null") != null);
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good", null, true, null, options, null);
}
public void testBug321926r() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       while ((someVariable = "not null") != null) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler buggy");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
		"Compiler good", null, true, null, options, null
		);
}
public void testBug321926s() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = " not null";
				       while ((someVariable = null) != null) {
							if (i == 0){
								someVariable = "not null";
								i++;
							}
							else
								throw new IOException();
						}
					 } catch (IOException e) {
						// broken from loop, continue on
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler good");
					 } else {
						System.out.println("Compiler buggy");
					 }
				  }
				}"""},
		"Compiler good", null, true, null, options, null
		);
}
public void testBug321926t() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
					public static void main(String s[]) {
						String file = "non null";
						int i = 0;
				       try {
							while (true) {
							    if (i == 0) {
									file = null;
				                   i++;
				               }
				               else\s
				               	throw new IOException();
							}
				       } catch (IOException e) {
				       }
						if (file == null)
						    System.out.println("Compiler good");
				       else\s
						    System.out.println("Compiler bad");
					}
				}
				"""},
		"Compiler good");
}
public void testBug321926u() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
					public static void main(String s[]) {
						String file = "non null";
						int i = 0;
				       try {
							while (true) {
							    if (i == 0) {
									file = null;
				                   i++;
				               }
				               else {
				                   file = null;
				               	throw new IOException();
				               }
							}
				       } catch (IOException e) {
				       }
						if (file == null)
						    System.out.println("Compiler good");
				       else\s
						    System.out.println("Compiler bad");
					}
				}
				"""},
		"Compiler good",
		options);
}
public void testBug321926v() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
					public static void main(String s[]) {
						String file = null;
						int i = 0;
				       try {
							while (true) {
							    if (i == 0) {
									file = "non null";
				                   i++;
				               }
				               else {
				                   file = "non null";
				               	throw new IOException();
				               }
							}
				       } catch (IOException e) {
				       }
						if (file == null)
						    System.out.println("Compiler bad");
				       else\s
						    System.out.println("Compiler good");
					}
				}
				"""},
		"Compiler good",
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						while (true) {
							throw new IOException();
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
// assignment from unknown - not reporting redundant check
public void testBug317829a2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = getString();
						while (true) {
							throw new IOException();
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				  static String getString() { return ""; }
				}"""},
			"Compiler good Compiler good");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						while (true) {
							someMethod();
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				  public static void someMethod() throws IOException {
				      throw new IOException();
				  }
				}"""},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829c() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						for (;;) {
							throw new IOException();
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
			"Compiler good Compiler good",
			options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829d() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						for(;;) {
							someMethod();
						}
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				  public static void someMethod() throws IOException {
				      throw new IOException();
				  }
				}"""},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829e() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						do {
							throw new IOException();
						} while (true);
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				}"""},
			"Compiler good Compiler good",
			options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=317829
public void testBug317829f() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.WARNING);
	options.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public static void main(String[] args) {
					 String someVariable = null;
					 int i = 0;
					 try {
				       someVariable = "not null";
						do {
							someMethod();
						} while (true);
					 } catch (IOException e) {
					 	if (someVariable == null) {
				    		System.out.println("Compiler bad");
					 	} else {
							System.out.print("Compiler good ");
					 	}
					 }
					 if (someVariable == null) {
				    	System.out.println("Compiler bad");
					 } else {
						System.out.println("Compiler good");
					 }
				  }
				  public static void someMethod() throws IOException {
				      throw new IOException();
				  }
				}"""},
			"Compiler good Compiler good",
			options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// LocalDeclaration
public void testBug292478() {
    this.runNegativeTest(
            new String[] {
                "X.java",
                "public class X {\n" +
                "  void foo(Object o) {\n" +
                "    if (o != null) {/* */}\n" +
                "    Object p = o;\n" +
                "    p.toString();\n" + // complain here
                "  }\n" +
                "}"},
            """
				----------
				1. ERROR in X.java (at line 5)
					p.toString();
					^
				Potential null pointer access: The variable p may be null at this location
				----------
				""",
            JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment
public void testBug292478a() {
  this.runNegativeTest(
          new String[] {
              "X.java",
              "public class X {\n" +
              "  void foo(Object o) {\n" +
              "    Object p;" +
              "    if (o != null) {/* */}\n" +
              "    p = o;\n" +
              "    p.toString();\n" + // complain here
              "  }\n" +
              "}"},
          """
			----------
			1. ERROR in X.java (at line 5)
				p.toString();
				^
			Potential null pointer access: The variable p may be null at this location
			----------
			""",
          JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment after definite null
public void testBug292478b() {
this.runNegativeTest(
        new String[] {
            "X.java",
            "public class X {\n" +
            "  void foo(Object o) {\n" +
            "    Object p = null;\n" +
            "    if (o != null) {/* */}\n" +
            "    p = o;\n" +
            "    p.toString();\n" + // complain here
            "  }\n" +
            "}"},
        """
			----------
			1. ERROR in X.java (at line 6)
				p.toString();
				^
			Potential null pointer access: The variable p may be null at this location
			----------
			""",
        JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment after definite null - many locals
public void testBug292478c() {
this.runNegativeTest(
      new String[] {
          "X.java",
          "public class X {\n" +
          "  void foo(Object o) {\n" +
          "    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;\n" +
          "    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;\n" +
          "    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;\n" +
          "    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;\n" +
          "    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;\n" +
          "    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;\n" +
          "    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;\n" +
          "    Object p = null;\n" +
          "    if (o != null) {/* */}\n" +
          "    p = o;\n" +
          "    p.toString();\n" + // complain here
          "  }\n" +
          "}"},
      """
		----------
		1. ERROR in X.java (at line 13)
			p.toString();
			^
		Potential null pointer access: The variable p may be null at this location
		----------
		""",
      JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// Assignment affects initsOnFinally
public void testBug292478d() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			" X bar() {\n" +
			"   return null;\n" +
			" }\n" +
			" Object foo() {\n" +
			"   X x = null;\n" +
			"   X y = new X();\n" +
			"   X u = null;\n" +
			"   try {\n" +
			"     u = bar();\n" +
			"     x = bar();\n" +
			"     if (x==null) { }\n" +
			"     y = x;\n" +				// this makes y potentially null
			"     if (x==null) { y=bar();} else { y=new X(); }\n" +
			"     return x;\n" +
			"   } finally {\n" +
			"     y.toString();\n" +		// must complain against potentially null, although normal exist of tryBlock says differently (unknown or non-null)
			"   }\n" +
			" }\n" +
			"}\n"},
		"""
			----------
			1. ERROR in X.java (at line 17)
				y.toString();
				^
			Potential null pointer access: The variable y may be null at this location
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// test regression reported in comment 8
public void testBug292478e() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object foo(int i, boolean b1, boolean b2) {
						Object o1 = null;
						done : while (true) {\s
							switch (i) {
								case 1 :
									Object o2 = null;
									if (b2)
										o2 = new Object();
									o1 = o2;
									break;
								case 2 :
									break done;
							}
						}	\t
						if (o1 != null)
							return o1;
						return null;
					}
				}
				"""
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// variant where regression occurred inside the while-switch structure
public void testBug292478f() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object foo(int i, boolean b1, boolean b2) {
						Object o1 = null;
						done : while (true) {\s
							switch (i) {
								case 1 :
									Object o2 = null;
									if (b2)
										o2 = new Object();
									o1 = o2;
									if (o1 != null)
										return o1;
									break;
								case 2 :
									break done;
							}
						}	\t
						return null;
					}
				}
				"""
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292478 -  Report potentially null across variable assignment
// variant for transfering state potentially unknown
public void testBug292478g() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					Object foo(int i, boolean b1, boolean b2, Object o2) {
						Object o1 = null;
						done : while (true) {\s
							switch (i) {
								case 1 :
									if (b2)
										o2 = bar();
									o1 = o2;
									if (o1 != null)
										return o1;
									break;
								case 2 :
									break done;
							}
						}	\t
						return null;
					}
				   Object bar() { return null; }
				}
				"""
		});
}

// Bug 324762 -  Compiler thinks there is deadcode and removes it!
// regression caused by the fix for bug 133125
// ternary is non-null or null
public void testBug324762() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					void zork(boolean b1) {
						Object satisfied = null;
						if (b1) {
							String[] s = new String[] { "a", "b" };
							for (int k = 0; k < s.length && satisfied == null; k++)
								satisfied = s.length > 1 ? new Object() : null;
						}
					}
				}
				"""
		});
}

// Bug 324762 -  Compiler thinks there is deadcode and removes it!
// regression caused by the fix for bug 133125
// ternary is unknown or null
public void testBug324762a() {
	this.runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					void zork(boolean b1) {
						Object satisfied = null;
						if (b1) {
							String[] s = new String[] { "a", "b" };
							for (int k = 0; k < s.length && satisfied == null; k++)
								satisfied = s.length > 1 ? bar() : null;
						}
					}
					Object bar() { return null; }
				}
				"""
		});
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// instancof expression
public void testBug325229a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						void foo(Object a) {
							assert a instanceof Object;
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null);
						}
					}
					"""},
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// MessageSend in assert
public void testBug325229b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						boolean bar() {
							return false;
						}\
						void foo(Test a) {
							assert a.bar();
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null);
						}
					}
					"""},
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// QualifiedNameReference in assert
public void testBug325229c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						boolean bar() {
							return false;
						}\
						Test tfield;
						void foo(Test a) {
							assert a.tfield.bar();
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null);
						}
					}
					"""},
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325229
// EqualExpression in assert, comparison against non null
public void testBug325229d() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						void foo(Object a) {
							Object b = null;\
							assert a == b;
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
							assert a != b;
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null);
						}
					}
					"""},
			"a is null\n" +
			"a is null",
			null,
			true,
			new String[] {"-da"},
			compilerOptions,
			null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
// Null warnings because of assert statements should be suppressed
// when CompilerOptions.OPTION_IncludeNullInfoFromAsserts is disabled.
public void testBug325342a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						void foo(Object a, Object b, Object c) {
							assert a == null;
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
							a = null;
							if (a== null) {}
							assert b != null;
					 \
							if (b!=null) {
								System.out.println("b is not null");
							 } else{
								System.out.println("b is null");
							 }
							assert c == null;
							if (c.equals(a)) {
								System.out.println("");
							 } else{
								System.out.println("");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null,null, null);
						}
					}
					"""},
			"""
				----------
				1. ERROR in Test.java (at line 10)
					if (a== null) {}
					    ^
				Redundant null check: The variable a can only be null at this location
				----------
				""",
			null,
			true,
			compilerOptions,
			"",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
// Null warnings because of assert statements should not be suppressed
// when CompilerOptions.OPTION_IncludeNullInfoFromAsserts is enabled.
public void testBug325342b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.ENABLED);
		this.runNegativeTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						void foo(Object a, Object b, Object c) {
							assert a == null;
					 \
							if (a!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
							assert b != null;
					 \
							if (b!=null) {
								System.out.println("a is not null");
							 } else{
								System.out.println("a is null");
							 }
							assert c == null;
							if (c.equals(a)) {
								System.out.println("");
							 } else{
								System.out.println("");
							 }
						}
						public static void main(String[] args){
							Test test = new Test();
							test.foo(null,null,null);
						}
					}
					"""},
			"""
				----------
				1. ERROR in Test.java (at line 4)
					if (a!=null) {
					    ^
				Null comparison always yields false: The variable a can only be null at this location
				----------
				2. WARNING in Test.java (at line 4)
					if (a!=null) {
							System.out.println("a is not null");
						 } else{
					             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				3. ERROR in Test.java (at line 10)
					if (b!=null) {
					    ^
				Redundant null check: The variable b cannot be null at this location
				----------
				4. WARNING in Test.java (at line 12)
					} else{
							System.out.println("a is null");
						 }
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				5. ERROR in Test.java (at line 16)
					if (c.equals(a)) {
					    ^
				Null pointer access: The variable c can only be null at this location
				----------
				""",
			null, true, compilerOptions, "",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325755
// null analysis -- conditional expression
public void testBug325755a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static Object foo(String s1, String s2) {
						String local1 = s1;
						String local2 = s2;
					\t
						String local3 = null;
						if (local1 != null && local2 != null)
							local3 = ""; //$NON-NLS-1$
						else
							local3 = local1 != null ? local1 : local2;
				
						if (local3 != null)
							return new Integer(local3.length());
						return null;
					}
				\t
					public static void main(String[] args) {
						System.out.print(foo(null, null));
						System.out.print(foo("p1", null));
						System.out.print(foo(null, "p2"));
						System.out.print(foo("p1", "p2"));
					}
				}"""},
		"null220");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325755
// null analysis -- conditional expression, many locals
public void testBug325755b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static Object foo(String s1, String s2) {
				    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;
				    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;
				    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;
				    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;
				    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;
				    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;
				    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;
						String local1 = s1;
						String local2 = s2;
					\t
						String local3 = null;
						if (local1 != null && local2 != null)
							local3 = ""; //$NON-NLS-1$
						else
							local3 = local1 != null ? local1 : local2;
				
						if (local3 != null)
							return new Integer(local3.length());
						return null;
					}
				\t
					public static void main(String[] args) {
						System.out.print(foo(null, null));
						System.out.print(foo("p1", null));
						System.out.print(foo(null, "p2"));
						System.out.print(foo("p1", "p2"));
					}
				}"""},
		"null220");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332637
// Dead Code detection removing code that isn't dead
public void testBug332637() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
		new String[] {
			"DeadCodeExample.java",
			"""
				public class DeadCodeExample {
				
					private class CanceledException extends Exception {
					}
				
					private interface ProgressMonitor {
						boolean isCanceled();
					}
				
					private void checkForCancellation(ProgressMonitor monitor)
							throws CanceledException {
						if (monitor.isCanceled()) {
							throw new CanceledException();
						}
					}
				
					private int run() {
				
						ProgressMonitor monitor = new ProgressMonitor() {
							private int i = 0;
				
							public boolean isCanceled() {
								return (++i == 5);
							}
						};
				
						Integer number = null;
				
						try {
							checkForCancellation(monitor);
				
							number = Integer.valueOf(0);
				
							for (String s : new String[10]) {
								checkForCancellation(monitor);
								number++;
							}
							return 0;
						} catch (CanceledException e) {
							System.out.println("Canceled after " + number
								+ " times through the loop");
							if (number != null) {
								System.out.println("number = " + number);
							}
							return -1;
						}
					}
				
					public static void main(String[] args) {
						System.out.println(new DeadCodeExample().run());
					}
				}
				"""
		},
		"""
			Canceled after 3 times through the loop
			number = 3
			-1""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332637
// Dead Code detection removing code that isn't dead
// variant with a finally block
public void testBug332637b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
		new String[] {
			"DeadCodeExample.java",
			"""
				public class DeadCodeExample {
				
					private class CanceledException extends Exception {
					}
				
					private interface ProgressMonitor {
						boolean isCanceled();
					}
				
					private void checkForCancellation(ProgressMonitor monitor)
							throws CanceledException {
						if (monitor.isCanceled()) {
							throw new CanceledException();
						}
					}
				
					private int run() {
				
						ProgressMonitor monitor = new ProgressMonitor() {
							private int i = 0;
				
							public boolean isCanceled() {
								return (++i == 5);
							}
						};
				
						Integer number = null;
				
						try {
							checkForCancellation(monitor);
				
							number = Integer.valueOf(0);
				
							for (String s : new String[10]) {
								checkForCancellation(monitor);
								number++;
							}
							return 0;
						} catch (CanceledException e) {
							System.out.println("Canceled after " + number
								+ " times through the loop");
							if (number != null) {
								System.out.println("number = " + number);
							}
							return -1;
						} finally {
							System.out.println("Done");
						}
					}
				
					public static void main(String[] args) {
						System.out.println(new DeadCodeExample().run());
					}
				}
				"""
		},
		"""
			Canceled after 3 times through the loop
			number = 3
			Done
			-1""");
}

public void testBug406160a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	this.runConformTest(
		new String[] {
			"DeadCodeExample.java",
			"""
				public class DeadCodeExample {
				
					class CanceledException extends Exception {
					}
				
					private interface ProgressMonitor {
						boolean isCanceled();
					}
				
					private void checkForCancellation(ProgressMonitor monitor)
							throws CanceledException {
						if (monitor.isCanceled()) {
							throw new CanceledException();
						}
					}
				
					private int run() {
				
						ProgressMonitor monitor = new ProgressMonitor() {
							private int i = 0;
				
							public boolean isCanceled() {
								return (++i == 5);
							}
						};
				
						Integer number = null;
				
						for (int j = 0; j < 1; ) {
				
							try {
								checkForCancellation(monitor);
				
								number = Integer.valueOf(0);
				
								for (String s : new String[10]) {
									checkForCancellation(monitor);
									number++;
								}
								return 0;
							} catch (CanceledException e) {
								System.out.println("Canceled after " + number
									+ " times through the loop");
								if (number != null) {
									System.out.println("number = " + number);
								}
								return -1;
							}
						}
						return 13;
					}
				
					public static void main(String[] args) {
						System.out.println(new DeadCodeExample().run());
					}
				}
				"""
		},
		"""
			Canceled after 3 times through the loop
			number = 3
			-1""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=333089
// null analysis -- to make sure no AIOOBE or NPE is thrown while calling UnconditionalFlowInfo.markNullStatus(..)
public void testBug333089() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo(Object s1) {
				    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;
				    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;
				    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;
				    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;
				    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;
				    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;
				    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;
					 Object local1;
					 if (s1 == null){}
					 try {\
						local1 = s1;
					 } finally {
					 }
					}
				}"""},
		"");
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//original issue
public void testBug336428() {
	this.runConformTest(
		new String[] {
	"DoWhileBug.java",
			"""
				public class DoWhileBug {
					void test(boolean b1, Object o1) {
						Object o2 = new Object();
						do {
				           if (b1)
								o1 = null;
						} while ((o2 = o1) != null);
					}
				}"""
		},
		"");
}
//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//hitting the same implementation branch from within the loop
//information from unknown o1 is not propagated into the loop, analysis currently believes o2 is def null.
public void _testBug336428a() {
	this.runConformTest(
		new String[] {
	"DoWhileBug.java",
			"""
				public class DoWhileBug {
					void test(boolean b1, Object o1) {
						Object o2 = null;
						do {
				           if (b1)
								o1 = null;
				           if ((o2 = o1) != null)
				               break;
						} while (true);
					}
				}"""
		},
		"");
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//in this variant the analysis believes o2 is def unknown and doesn't even consider raising a warning.
public void _testBug336428b() {
	runNegativeNullTest(
		new String[] {
	"DoWhileBug.java",
			"""
				public class DoWhileBug {
					void test(boolean b1) {
						Object o1 = null;
						Object o2 = null;
						do {
				           if ((o2 = o1) == null) break;
						} while (true);
					}
				}"""
		},
		"""
			----------
			1. ERROR in DoWhileBug.java (at line 6)
				if ((o2 = o1) == null) break;
				    ^^^^^^^^^
			Redundant null check: The variable o2 can only be null at this location
			----------
			""");
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//in this case considering o1 as unknown is correct
public void testBug336428c() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
		"DoWhileBug.java",
				"""
					public class DoWhileBug {
						void test(boolean b1, Object o1) {
							Object o2 = null;
							do {
					           if ((o2 = o1) == null) break;
							} while (true);
						}
					}"""
			},
			"");
	}
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//one more if-statement triggers the expected warnings
public void testBug336428d() {
	runNegativeNullTest(
		new String[] {
	"DoWhileBug.java",
			"""
				public class DoWhileBug {
					void test(boolean b1) {
						Object o1 = null;
						Object o2 = null;
						do {
				           if (b1)
								o1 = null;
				           if ((o2 = o1) == null) break;
						} while (true);
					}
				}"""
		},
		"----------\n" +
		"1. ERROR in DoWhileBug.java (at line 7)\n" +
		"	o1 = null;\n" +
		"	^^\n" +
		"Redundant assignment: The variable o1 can only be null at this location\n" +
/* In general it's safer *not* to assume that o1 is null on every iteration (see also testBug336428d2):
		"----------\n" +
		"2. ERROR in DoWhileBug.java (at line 8)\n" +
		"	if ((o2 = o1) == null) break;\n" +
		"	    ^^^^^^^^^\n" +
		"Redundant null check: The variable o2 can only be null at this location\n" +
 */
		"----------\n"
		);
}

// Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
// variant after Bug 454031 to demonstrate:
// - previously we would believe that o1 is always null in the assignment to o2 -> bogus warning re redundant null check
// - with improved analysis we don't claim to know the value of o1 in this assignment -> no warning
public void testBug336428d2() {
	this.runConformTest(
		new String[] {
	"DoWhileBug.java",
			"""
				public class DoWhileBug {
					void test(boolean b1) {
						Object o1 = null;
						Object o2 = null;
						do {
				           if (b1)
								o1 = null;
				           if ((o2 = o1) == null) System.out.println("null");
							o1 = new Object();
						} while (true);
					}
				}"""
		});
}

//Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
//same analysis, but assert instead of if suppresses the warning
public void testBug336428e() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
		"DoWhileBug.java",
				"""
					public class DoWhileBug {
						void test(boolean b1) {
							Object o1 = null;
							Object o2 = null;
							do {
					           if (b1)
									o1 = null;
					           assert (o2 = o1) != null : "bug";
							} while (true);
						}
					}"""
			},
			"----------\n" +
			"1. ERROR in DoWhileBug.java (at line 7)\n" +
			"	o1 = null;\n" +
			"	^^\n" +
			"Redundant assignment: The variable o1 can only be null at this location\n" +
/* In general it's safer *not* to assume that o1 is null on every iteration:
			"----------\n" +
			"2. ERROR in DoWhileBug.java (at line 8)\n" +
			"	assert (o2 = o1) != null : \"bug\";\n" +
			"	       ^^^^^^^^^\n" +
			"Null comparison always yields false: The variable o2 can only be null at this location\n" +
 */
			"----------\n");
	}
}

// Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
// same analysis, but assert instead of if suppresses the warning
// condition inside assert is redundant null check and hence should not be warned against
public void testBug336428f() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
		"DoWhileBug.java",
				"""
					public class DoWhileBug {
						void test(boolean b1) {
							Object o1 = null;
							Object o2 = null;
							do {
					           if (b1)
									o1 = null;
					           assert (o2 = o1) == null : "bug";
							} while (true);
						}
					}"""
			},
			"""
				----------
				1. ERROR in DoWhileBug.java (at line 7)
					o1 = null;
					^^
				Redundant assignment: The variable o1 can only be null at this location
				----------
				""");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=332838
// Null info of assert statements should not affect flow info
// when CompilerOptions.OPTION_IncludeNullInfoFromAsserts is disabled.
public void testBug332838() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map compilerOptions = getCompilerOptions();
		compilerOptions.put(CompilerOptions.OPTION_IncludeNullInfoFromAsserts, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"Info.java",
				"public class Info {\n" +
				"	public void test(Info[] infos) {\n" +
				"		for (final Info info : infos) {\n " +
				"			if (info != null) {\n" +
				"				assert info.checkSomething();\n" +
				"		 		info.doSomething();\n" +	// no warning
				"			}\n" +
				"		 }\n" +
				"		for (final Info info : infos) {\n " +
				"			if (info == null) {\n" +
				"				assert info.checkSomething();\n" +
				"		 		info.doSomething();\n" +	// warn NPE, not pot. NPE
				"			}\n" +
				"		 }\n" +
				"	}\n" +
				"	void doSomething()  {}\n" +
				"	boolean checkSomething() {return true;}\n" +
				"}\n"},
			"""
				----------
				1. ERROR in Info.java (at line 11)
					assert info.checkSomething();
					       ^^^^
				Null pointer access: The variable info can only be null at this location
				----------
				2. ERROR in Info.java (at line 12)
					info.doSomething();
					^^^^
				Null pointer access: The variable info can only be null at this location
				----------
				""",
			null,
			true,
			compilerOptions,
			"",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336544
public void testBug336544() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer i1 = getInt();
						Integer i2 = i1 == null ? null : i1;
						if (i2 != null) {
							System.out.println("SUCCESS");
							return;
						}
						System.out.println("FAILURE");
					}
					private static Integer getInt() {
						return new Integer(0);
					}
				}"""
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336544
public void testBug336544_2() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer i1 = null;
						Integer i2 = (i1 = getInt()) == null ? null : i1;
						if (i2 != null) {
							System.out.println("SUCCESS");
							return;
						}
						System.out.println("FAILURE");
					}
					private static Integer getInt() {
						return new Integer(0);
					}
				}"""
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336544
public void testBug336544_3() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer i1 = null;
						Integer i2;
						i2 = (i1 = getInt()) == null ? null : i1;
						if (i2 != null) {
							System.out.println("SUCCESS");
							return;
						}
						System.out.println("FAILURE");
					}
					private static Integer getInt() {
						return new Integer(0);
					}
				}"""
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313870
public void testBug313870() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = "";
						for (int i = 0; i < 2; i++) {
							if (i != 0) {\s
				    			s = test();
							}
							if (s == null) {
				    			System.out.println("null");
							}
						}
					}
					public static String test() {
						return null;
					}
				}"""
		},
		"null");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313870
public void testBug313870b() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.BufferedReader;
				import java.io.IOException;
				public class X {
					public void main(BufferedReader bufReader) throws IOException {
						String line = "";
						boolean doRead = false;
						while (true) {
							if (doRead) {\s
				    		   line = bufReader.readLine();
							}
							if (line == null) {
				    			return;
							}
							doRead = true;
						}
					}
				}"""
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=313870
public void testBug313870c() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				public class X {
					public static void main(String[] args) {
						boolean sometimes = (System.currentTimeMillis() & 1L) != 0L;
						File file = new File("myfile");
						for (int i = 0; i < 2; i++) {
							if (sometimes) {\s
				    		 	file = getNewFile();
							}
							if (file == null) {\s
				    			System.out.println("");
							}
						}
					}
					private static File getNewFile() {
						return null;
					}
				}"""
		},
		"");
}
// https://bugs.eclipse.org/338303 - Warning about Redundant assignment conflicts with definite assignment
public void testBug338303() {
	this.runConformTest(
		new String[] {
			"Bug338303.java",
			"""
				import java.io.File;
				import java.io.IOException;
				
				public class Bug338303 {
				   Object test(Object in, final File f) {
				        Object local;
				        try {
				            local = in;
				            if (local == null)
				                local = loadEntry(f, false);
				        } catch (final IOException e) {
				            e.printStackTrace();
				            local = null;
				        }
				        return local;
				    }
				
				    private Object loadEntry(File f, boolean b)  throws IOException {
				        throw new IOException();
				    }
				}
				"""
		},
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338234
public void testBug338234() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				   static int foo() {
				        Object o = null;
						 int i = 0;
				        label: {
				            if (o == null)
				                break label;
							 i++;\
				        }
				         if (i != 0) {
				            System.out.println(i);
				        }
				        return 0;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (o == null)
				    ^
			Redundant null check: The variable o can only be null at this location
			----------
			2. WARNING in X.java (at line 8)
				i++;        }
				^^^
			Dead code
			----------
			""");
}
// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
public void testBug324178() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"""
				public class Bug324178 {
				    boolean b;
				    void foo(Object u) {
				    if (u == null) {}
				        Object o = (u == null) ? new Object() : u;
				        o.toString();   // Incorrect potential NPE
				    }
				}
				"""
		},
		"");
}

// Bug 324178 - [null] ConditionalExpression.nullStatus(..) doesn't take into account the analysis of condition itself
public void testBug324178a() {
	this.runConformTest(
		new String[] {
			"Bug324178.java",
			"""
				public class Bug324178 {
				    boolean b;
				    void foo(Boolean u) {
				    if (u == null) {}
				        Boolean o;
				        o = (u == null) ? Boolean.TRUE : u;
				        o.toString();   // Incorrect potential NPE
				    }
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
public void testBug326950a() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s = null;
						if (s == null) {
							System.out.println("SUCCESS");
						} else {
							System.out.println("Dead code, but don't optimize me out");
						}
					}
				}""",
		},
		"SUCCESS",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"""
		  public static void main(java.lang.String[] args);
		     0  aconst_null
		     1  astore_1 [s]
		     2  aload_1 [s]
		     3  ifnonnull 17
		     6  getstatic java.lang.System.out : java.io.PrintStream [16]
		     9  ldc <String "SUCCESS"> [22]
		    11  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    14  goto 25
		    17  getstatic java.lang.System.out : java.io.PrintStream [16]
		    20  ldc <String "Dead code, but don't optimize me out"> [30]
		    22  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]
		    25  return
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
// Code marked dead due to if(false), etc. can be optimized out
public void testBug326950b() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						int i = 0;
						if (false) {
							System.out.println("Deadcode and you can optimize me out");
						}
						if (true) {
							i++;
						} else {
							System.out.println("Deadcode and you can optimize me out");
						}
					}
				}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"""
		  public static void main(java.lang.String[] args);
		    0  iconst_0
		    1  istore_1 [i]
		    2  iinc 1 1 [i]
		    5  return
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
// Free return should be generated for a method even if it ends with dead code
public void testBug326950c() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo(String[] args) {
						String s = "";
						int i = 0;
						if (s != null) {
							return;
						}
						i++;
					}
				}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"""
		  public void foo(java.lang.String[] args);
		     0  ldc <String ""> [16]
		     2  astore_2 [s]
		     3  iconst_0
		     4  istore_3 [i]
		     5  aload_2 [s]
		     6  ifnull 10
		     9  return
		    10  iinc 3 1 [i]
		    13  return
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326950
// Free return should be generated for a constructor even if it ends with dead code
public void testBug326950d() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportPotentialNullReference, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X() {
						String s = "";
						int i = 0;
						if (s != null) {
							return;
						}
						i++;
					}
				}""",
		},
		"",
		null,
		true,
		null,
		options,
		null);
	String expectedOutput =
		"""
		  X();
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  ldc <String ""> [10]
		     6  astore_1 [s]
		     7  iconst_0
		     8  istore_2 [i]
		     9  aload_1 [s]
		    10  ifnull 14
		    13  return
		    14  iinc 2 1 [i]
		    17  return
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
// Check code gen
public void testBug339250() throws Exception {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		s += \"correctly\";\n" +
			"		if (s != null) {\n" + 	// s cannot be null
			"			System.out.println(\"It works \" + s);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"It works nullcorrectly",
		null,
		true,
		null,
		options,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
// Check that the redundant null check warning is correctly produced
public void testBug339250a() throws Exception {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		s += \"correctly\";\n" +
			"		if (s != null) {\n" + 	// s cannot be null
			"			System.out.println(\"It works \" + s);\n" +
			"		}\n" +
			"	}\n" +
			"}",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (s != null) {
				    ^
			Redundant null check: The variable s cannot be null at this location
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
// Check that the redundant null check warning is correctly produced
public void testBug339250b() throws Exception {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		String s = null;\n" +
			"		s += null;\n" +
			"		if (s != null) {\n" + 	// s is definitely not null
			"			System.out.println(\"It works \" + s);\n" +
			"	    }\n" +
			"		s = null;\n" +
			"		if (s != null) {\n" + 	// s is definitely null
			"			System.out.println(\"Fails \" + s);\n" +
			"	    } else {\n" +
			"			System.out.println(\"Works second time too \" + s);\n" +
			"       }\n" +
			"	}\n" +
			"}",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (s != null) {
				    ^
			Redundant null check: The variable s cannot be null at this location
			----------
			2. ERROR in X.java (at line 9)
				if (s != null) {
				    ^
			Null comparison always yields false: The variable s can only be null at this location
			----------
			3. WARNING in X.java (at line 9)
				if (s != null) {
						System.out.println("Fails " + s);
				    } else {
				               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342300
public void testBug342300() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void initPattern(String p, Character escapeChar) {\n" +
				"		int len = p.length();\n" +
				"		for (int i = 0; i < len; i++) {\n" +
				"			char c = p.charAt(i);\n" +
				"			if (escapeChar != null && escapeChar == c) {\n" +	// quiet
				"				c = p.charAt(++i);\n" +
				"			}\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=342300
// To make sure only the redundant null check is given and not a potential NPE
public void testBug342300b() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void initPattern(String p, Character escapeChar) {\n" +
				"		int len = p.length();\n" +
				"		for (int i = 0; i < len; i++) {\n" +
				"			char c = p.charAt(i);\n" +
				"			if (escapeChar != null && escapeChar != null) {\n" +	// look here
				"				c = p.charAt(++i);\n" +
				"			}\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					if (escapeChar != null && escapeChar != null) {
					                          ^^^^^^^^^^
				Redundant null check: The variable escapeChar cannot be null at this location
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379a() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo() {
							String s = null;
							switch(s) {
							case "abcd":
								System.out.println("abcd");
								break;
							default:
								System.out.println("oops");
								break;
						    }
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					switch(s) {
					       ^
				Null pointer access: The variable s can only be null at this location
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379b() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		String s = \"abcd\";\n" +
				"		switch(s) {\n" +	// no warning since s is not null
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"abcd");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379c() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		if (s == null) {}\n" +		// tainting s
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					switch(s) {
					       ^
				Potential null pointer access: The variable s may be null at this location
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379d() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		if (s != null) {}\n" +		// tainting s
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					switch(s) {
					       ^
				Potential null pointer access: The variable s may be null at this location
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379e() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		if (s == null) {}\n" +		// tainting s
				"		else\n" +
				"		switch(s) {\n" +   // no warning because we're inside else
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			System.out.println(\"oops\");\n" +
				"			break;\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348379
public void testBug348379f() throws Exception {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public void foo(String s) {\n" +
				"		s = null;\n" +
				"		switch(s) {\n" +
				"		case \"abcd\":\n" +
				"			System.out.println(\"abcd\");\n" +
				"			break;\n" +
				"		default:\n" +
				"			switch(s) {\n" +	// do not warn again
				"				case \"abcd\":\n" +
				"					System.out.println(\"abcd\");\n" +
				"					break;\n" +
				"				default:\n" +
				"					break;\n" +
				"			}\n" +
				"	    }\n" +
				"	}\n" +
				"}",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					switch(s) {
					       ^
				Null pointer access: The variable s can only be null at this location
				----------
				""");
	}
}
// Bug 354554 - [null] conditional with redundant condition yields weak error message
public void testBug354554() {
	runNegativeNullTest(
		new String[] {
			"Bug354554.java",
			"public class Bug354554{\n" +
			"    void foo() {\n" +
			"        Object u = new Object();\n" +
			"        Object r = (u == null ? u : null);\n" + // condition is always false - should not spoil subsequent null-analysis
			"        System.out.println(r.toString());\n" +  // should strongly complain: r is definitely null
			"    }\n" +
			"}\n"
		},
		"""
			----------
			1. ERROR in Bug354554.java (at line 4)
				Object r = (u == null ? u : null);
				            ^
			Null comparison always yields false: The variable u cannot be null at this location
			----------
			2. ERROR in Bug354554.java (at line 5)
				System.out.println(r.toString());
				                   ^
			Null pointer access: The variable r can only be null at this location
			----------
			""");
}
//Bug 354554 - [null] conditional with redundant condition yields weak error message
public void testBug354554b() {
	runNegativeNullTest(
		new String[] {
			"Bug354554.java",
			"public class Bug354554{\n" +
			"    void foo() {\n" +
			"        Object u = new Object();\n" +
			"        Object r = (u != null ? u : null);\n" + // condition is always true - should not spoil subsequent null-analysis
			"        System.out.println(r.toString());\n" +  // don't complain: r is definitely non-null
			"    }\n" +
			"}\n"
		},
		"""
			----------
			1. ERROR in Bug354554.java (at line 4)
				Object r = (u != null ? u : null);
				            ^
			Redundant null check: The variable u cannot be null at this location
			----------
			""");
}
// Bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
public void test358827() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		runNegativeNullTest(
				new String[] {
					"Bug358827.java",
					"""
						import java.io.FileReader;
						public class Bug358827 {
							Object foo2() throws Exception {
								String o = null;
								try (FileReader rf = new FileReader("file")){
									o = o.toUpperCase();
								} finally {
									o = "OK";
								}
								return o;
							}
						}
						"""
				},
				"""
					----------
					1. ERROR in Bug358827.java (at line 6)
						o = o.toUpperCase();
						    ^
					Null pointer access: The variable o can only be null at this location
					----------
					""");
	}
}
// Bug 367879 - Incorrect "Potential null pointer access" warning on statement after try-with-resources within try-finally
public void test367879() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7) {
		this.runConformTest(
				new String[] {
					"Bug367879.java",
					"import java.io.IOException;\n" +
					"import java.io.InputStream;\n" +
					"import java.net.HttpURLConnection;\n" +
					"import java.net.URL;\n" +
					"public class Bug367879 {\n" +
					"    public void test() throws IOException {\n" +
					"    HttpURLConnection http = null;\n" +
					"        try {\n" +
					"            http = (HttpURLConnection) new URL(\"http://example.com/\").openConnection();\n" +
					"            try (InputStream in = http.getInputStream()) { /* get input */ }\n" +
					"            http.getURL();\n" + // shouldn't *not* flag as Potential null pointer access
					"        } finally {\n" +
					"            if (http != null) { http.disconnect(); }\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
				},
				"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=256796
public void testBug256796() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.DISABLED);
	this.runNegativeTest(
			new String[] {
				"Bug.java",
				"public class Bug {\n" +
				"	private static final boolean TRUE = true;\n" +
				"   private static final boolean FALSE = false;\n" +
				"	void foo() throws Exception {\n" +
				"		if (TRUE) return;\n" +
				"		else System.out.println(\"\");\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (TRUE) throw new Exception();\n" +
				"		else System.out.println(\"\");\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (TRUE) return;\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (FALSE) System.out.println(\"\");\n" +
				"		else return;\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (FALSE) return;\n" +
				"		System.out.println(\"\");\n" + 		// not dead code
				"		if (false) return;\n" +				// dead code
				"		System.out.println(\"\");\n" +
				"		if (true) return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"}\n"
			},
			"""
				----------
				1. WARNING in Bug.java (at line 18)
					if (false) return;
					           ^^^^^^^
				Dead code
				----------
				2. WARNING in Bug.java (at line 21)
					System.out.println("");
					^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				""",
			null,
			true,
			compilerOptions,
			null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=256796
public void testBug256796a() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.WARNING);
	compilerOptions.put(CompilerOptions.OPTION_ReportDeadCodeInTrivialIfStatement, CompilerOptions.ENABLED);
	this.runNegativeTest(
			new String[] {
				"Bug.java",
				"public class Bug {\n" +
				"	private static final boolean TRUE = true;\n" +
				"   private static final boolean FALSE = false;\n" +
				"	void foo() throws Exception {\n" +
				"		if (TRUE) return;\n" +
				"		else System.out.println(\"\");\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"   void foo2() {\n" +
				"		if (TRUE) return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"	void foo3() throws Exception {\n" +
				"		if (TRUE) throw new Exception();\n" +
				"		else System.out.println(\"\");\n" + // dead code
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"	void foo4() throws Exception {\n" +
				"		if (FALSE) System.out.println(\"\");\n" +
				"		else return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"	void foo5() throws Exception {\n" +
				"		if (FALSE) return;\n" +				// dead code
				"		System.out.println(\"\");\n" +
				"	}\n" +
				"	void foo6() throws Exception {\n" +
				"		if (false) return;\n" +				// dead code
				"		System.out.println(\"\");\n" +
				"		if (true) return;\n" +
				"		System.out.println(\"\");\n" + 		// dead code
				"	}\n" +
				"}\n"
			},
			"""
				----------
				1. WARNING in Bug.java (at line 6)
					else System.out.println("");
					     ^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				2. WARNING in Bug.java (at line 7)
					System.out.println("");
					^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				3. WARNING in Bug.java (at line 11)
					System.out.println("");
					^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				4. WARNING in Bug.java (at line 15)
					else System.out.println("");
					     ^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				5. WARNING in Bug.java (at line 16)
					System.out.println("");
					^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				6. WARNING in Bug.java (at line 19)
					if (FALSE) System.out.println("");
					           ^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				7. WARNING in Bug.java (at line 21)
					System.out.println("");
					^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				8. WARNING in Bug.java (at line 24)
					if (FALSE) return;
					           ^^^^^^^
				Dead code
				----------
				9. WARNING in Bug.java (at line 28)
					if (false) return;
					           ^^^^^^^
				Dead code
				----------
				10. WARNING in Bug.java (at line 31)
					System.out.println("");
					^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				""",
			null,
			true,
			compilerOptions,
			"",
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
public void testBug360328() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"""
				public class X {
				    void print4() {
				        final String s1 = "";
				        for (int i=0; i<4; i++)
				            new Runnable() {
				                public void run() {
				                     if (s1 != null)
				                         s1.toString();
				                }
				            }.run();
				    }
				    void print16(boolean b) {
				        final String s3 = b ? null : "";
				        for (int i=0; i<16; i++)
				            new Runnable() {
				                public void run() {
				                     s3.toString();
				                }
				            }.run();
				    }
				    void print23() {
				        final String s23 = null;
				        for (int i=0; i<23; i++)
				            new Runnable() {
				                public void run() {
				                     s23.toString();
				                }
				            }.run();
				    }
				}
				""",

		},
		null, /* classLibs */
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (s1 != null)
				    ^^
			Redundant null check: The variable s1 cannot be null at this location
			----------
			2. ERROR in X.java (at line 17)
				s3.toString();
				^^
			Potential null pointer access: The variable s3 may be null at this location
			----------
			3. ERROR in X.java (at line 26)
				s23.toString();
				^^^
			Null pointer access: The variable s23 can only be null at this location
			----------
			""",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// constructors
public void testBug360328b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"""
				public class X {
				    void print4() {
				        final String s1 = "";
				        for (int i=0; i<4; i++) {
				            class R {
				                public R() {
				                     if (s1 != null)
				                         s1.toString();
				                }
				            };
				            new R();
				        }
				    }
				    void print16(boolean b) {
				        final String s3 = b ? null : "";
				        int i=0; while (i++<16) {
				            class R {
				                public R() {
				                     s3.toString();
				                }
				            };
				            new R();
				        };
				    }
				    void print23() {
				        final String s23 = null;
				        for (int i=0; i<23; i++) {
				            class R {
				                public R() {
				                     s23.toString();
				                }
				            };
				            new R();
				        };
				    }
				}
				""",

		},
		null, /* classLibs */
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (s1 != null)
				    ^^
			Redundant null check: The variable s1 cannot be null at this location
			----------
			2. ERROR in X.java (at line 19)
				s3.toString();
				^^
			Potential null pointer access: The variable s3 may be null at this location
			----------
			3. ERROR in X.java (at line 30)
				s23.toString();
				^^^
			Null pointer access: The variable s23 can only be null at this location
			----------
			""",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// initializers
public void testBug360328c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"""
				public class X {
				    void print4() {
				        final String s1 = "";
				        for (int i=0; i<4; i++) {
				            class R {
				                String s1R;
				                {
				                    if (s1 != null)
				                         s1R = s1;
				                }
				            };
				            new R();
				        }
				    }
				    void print16(boolean b) {
				        final String s3 = b ? null : "";
				        for (int i=0; i<16; i++) {
				            class R {
				                String s3R = s3.toString();
				            };
				            new R();
				        };
				    }
				    void print23() {
				        final String s23 = null;
				        for (int i=0; i<23; i++) {
				            class R {
				                String s23R;
				                {
				                     s23R = s23.toString();
				                }
				            };
				            new R();
				        };
				    }
				}
				""",

		},
		null, /* classLibs */
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 8)
				if (s1 != null)
				    ^^
			Redundant null check: The variable s1 cannot be null at this location
			----------
			2. ERROR in X.java (at line 19)
				String s3R = s3.toString();
				             ^^
			Potential null pointer access: The variable s3 may be null at this location
			----------
			3. ERROR in X.java (at line 30)
				s23R = s23.toString();
				       ^^^
			Null pointer access: The variable s23 can only be null at this location
			----------
			""",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
// try-finally instead of loop
public void testBug360328d() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportRedundantNullCheck, CompilerOptions.ERROR);
	runNegativeTest(
		true, /* flushOutputDir */
		new String[] {
			"X.java",
			"""
				public class X {
				    void print4() {
				        final String s1 = "";
				        try { } finally {
				            new Runnable() {
				                public void run() {
				                     if (s1 != null)
				                         s1.toString();
				                }
				            }.run();
				        }
				    }
				    void print16(boolean b) {
				        final String s3 = b ? null : "";
				        try { } finally {
				            new Runnable() {
				                public void run() {
				                     s3.toString();
				                }
				            }.run();
				        }
				    }
				    void print23() {
				        final String s23 = null;
				        try { } finally {
				            new Runnable() {
				                public void run() {
				                     s23.toString();
				                }
				            }.run();
				        }
				    }
				}
				""",

		},
		null, /* classLibs */
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 7)
				if (s1 != null)
				    ^^
			Redundant null check: The variable s1 cannot be null at this location
			----------
			2. ERROR in X.java (at line 18)
				s3.toString();
				^^
			Potential null pointer access: The variable s3 may be null at this location
			----------
			3. ERROR in X.java (at line 28)
				s23.toString();
				^^^
			Null pointer access: The variable s23 can only be null at this location
			----------
			""",
		"",/* expected output */
		"",/* expected error */
	    JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 384380 - False positive on a "Potential null pointer access" after a continue
// original test case
public void testBug384380() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						public static class Container{
							public int property;
						}
						public static class CustomException extends Exception {
							private static final long	 serialVersionUID	= 1L;
						}
						public static void anotherMethod() throws CustomException {}
					
						public static void method(final java.util.List<Container> list) {
							for (final Container c : list) {
								if(c == null)
									continue; // return or break, are fine though
					
								// without this try-catch+for+exception block it does not fails
								try {
									for(int i = 0; i < 10 ; i++) // needs a loop here (a 'while' or a 'for') to fail
										anotherMethod(); // throwing directly CustomException make it fails too
								} catch (final CustomException e) {
									// it fails even if catch is empty
								}
								c.property += 1; // "Potential null pointer access: The variable c may be null at this location"
							}
					
						}
					}
					"""
			},
			"");
	}
}
// Bug 384380 - False positive on a "Potential null pointer access" after a continue
// variant with a finally block
public void testBug384380_a() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						public static class Container{
							public int property;
						}
						public static class CustomException extends Exception {
							private static final long	 serialVersionUID	= 1L;
						}
						public static void anotherMethod() throws CustomException {}
					
						public static void method(final java.util.List<Container> list) {
							for (final Container c : list) {
								if(c == null)
									continue; // return or break, are fine though
					
								// without this try-catch+for+exception block it does not fails
								try {
									for(int i = 0; i < 10 ; i++) // needs a loop here (a 'while' or a 'for') to fail
										anotherMethod(); // throwing directly CustomException make it fails too
								} catch (final CustomException e) {
									// it fails even if catch is empty
								} finally {
									System.out.print(3);
								}
								c.property += 1; // "Potential null pointer access: The variable c may be null at this location"
							}
					
						}
					}
					"""
			},
			"");
	}
}
// Bug 384380 - False positive on a "Potential null pointer access" after a continue
// while & foreach loops
public void testBug384380_b() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"Test.java",
				"""
					public class Test {
						public static class Container{
							public int property;
						}
						public static class CustomException extends Exception {
							private static final long	 serialVersionUID	= 1L;
						}
						public static void anotherMethod() throws CustomException {}
					
						public static void method(final java.util.List<Container> list) {
							java.util.Iterator<Container> it = list.iterator();
							while (it.hasNext()) {
								final Container c = it.next();
								if(c == null)
									continue; // return or break, are fine though
					
								// without this try-catch+for+exception block it does not fails
								try {
									for(Container c1 : list) // needs a loop here (a 'while' or a 'for') to fail
										anotherMethod(); // throwing directly CustomException make it fails too
								} catch (final CustomException e) {
									// it fails even if catch is empty
								}
								c.property += 1; // "Potential null pointer access: The variable c may be null at this location"
							}
					
						}
					}
					"""
			},
			"");
	}
}
public void testBug376263() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    private int x;
				
				    static void test(Test[] array) {
				        Test elem = null;
				        int i = 0;
				        while (i < array.length) {
				            if (i == 0) {
				                elem = array[0];
				            }
				            if (elem != null) {
				                while (true) {
				                    if (elem.x >= 0 || i >= array.length) { // should not warn here
				                        break;
				                    }
				                    elem = array[i++];
				                }
				            }
				        }
				    }
				}"""
		},
		"",
		null/*classLibraries*/,
		true/*shouldFlush*/,
		null/*vmArgs*/,
		customOptions,
		null/*requestor*/);
}
//object/array allocation
public void testExpressions01() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
					 void foo() {
						if (new Object() == null)
				           System.out.println("null");
				    }
					 void goo() {
						if (null != this.new I())
				           System.out.println("nonnull");
				    }
				    void hoo() {
						if (null != new Object[3])
				           System.out.println("nonnull");
				    }
				    class I {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (new Object() == null)
				    ^^^^^^^^^^^^
			Null comparison always yields false: this expression cannot be null
			----------
			2. WARNING in X.java (at line 4)
				System.out.println("null");
				^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 7)
				if (null != this.new I())
				            ^^^^^^^^^^^^
			Redundant null check: this expression cannot be null
			----------
			4. ERROR in X.java (at line 11)
				if (null != new Object[3])
				            ^^^^^^^^^^^^^
			Redundant null check: this expression cannot be null
			----------
			"""
	);
}
//'this' expressions (incl. qualif.)
public void testExpressions02() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
					 void foo() {
						if (this == null)
				           System.out.println("null");
				    }
				    class I {
				        void goo() {
						     if (null != X.this)
				                System.out.println("nonnull");
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (this == null)
				    ^^^^
			Null comparison always yields false: this expression cannot be null
			----------
			2. WARNING in X.java (at line 4)
				System.out.println("null");
				^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 8)
				if (null != X.this)
				            ^^^^^^
			Redundant null check: this expression cannot be null
			----------
			"""
	);
}
//various non-null expressions: class-literal, string-literal, casted 'this'
public void testExpressions03() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
					 void foo() {
						if (X.class == null)
				           System.out.println("null");
				    }
				    void goo() {
				        if (null != "STRING")
				            System.out.println("nonnull");
				        if (null == (Object)this)
				            System.out.println("I'm null");
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (X.class == null)
				    ^^^^^^^
			Null comparison always yields false: this expression cannot be null
			----------
			2. WARNING in X.java (at line 4)
				System.out.println("null");
				^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 7)
				if (null != "STRING")
				            ^^^^^^^^
			Redundant null check: this expression cannot be null
			----------
			4. ERROR in X.java (at line 9)
				if (null == (Object)this)
				            ^^^^^^^^^^^^
			Null comparison always yields false: this expression cannot be null
			----------
			5. WARNING in X.java (at line 10)
				System.out.println("I'm null");
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			"""
	);
}

//a non-null ternary expression
public void testExpressions04() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(boolean b) {
						Object o1 = new Object();
						Object o2 = new Object();
						if ((b ? o1 : o2) != null)
							System.out.println("null");
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				if ((b ? o1 : o2) != null)
				    ^^^^^^^^^^^^^
			Redundant null check: this expression cannot be null
			----------
			"""
	);
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// simplified: only try-finally involved
public void testBug345305_1() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo() {
				        String s = null;
				        try {
				            s = "hi";
				        } finally {
				            s.length();
				            s = null;
				        }
				    }
				}
				"""
		});
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// original test case
public void testBug345305_2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo() {
				        String s = null;
				        while (true) {
				            try {
				                s = "hi";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		});
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// assignment in method argument position
public void testBug345305_3() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo() {
				        String s = null;
				        while (true) {
				            try {
				                check(s = "hi");
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				    void check(String s) {}
				}
				"""
		});
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// analysis of second local variable must not interfere
public void testBug345305_4() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo() {
				        String s = "";
				        String s2 = null;
				        while (true) {
				            try {
				                s = null;
				                bar();
				                s2 = "world";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				    void bar() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				s.length();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// block-less if involved - info about pot.nn. was lost when checking against loop's info (deferred check)
public void testBug345305_6() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(boolean b) {
				        String s = null;
				        while (true) {
				            try {
				                if (b)
				                    s = "hi";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// block-less if involved
public void testBug345305_7() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(boolean b) {
				        while (true) {
				            String s = null;
				            try {
				                if (b)
				                    s = "hi";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// consider exception thrown from cast expression
public void testBug345305_8() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(Object o) {
				        while (true) {
				            String s = null;
				            try {
				                 s = (String) o;
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// consider exception thrown from binary expression
public void testBug345305_9() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(int i, int j) {
				        while (true) {
				            String s = null;
				            try {
				                 s = ((i / j) == 3) ? "3" : "not-3";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// inner labeled block with break
public void testBug345305_10() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(int j) {
				        while (true) {
				            String s = null;
				            try {
				                int i=0;
				                block: {
				                    if (i++ == j)
				                         break block;
				                    s = "";
				                    return;
				                }
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// switch statement
public void testBug345305_11() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(int j) {
				        while (true) {
				            String s = null;
				            try {
				                switch (j) {
				                    case 3:
				                        s = "";
				                        return;
				                    default: return;
				                }
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// assignment inside conditional expression
public void testBug345305_12() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    String foo(boolean b) {
				        while (true) {
				            String s = null;
				            try {
				                 return b ? (s = "be") : "be not";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// explicit throw
public void testBug345305_13() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    String foo(boolean b) {
				        while (true) {
				            String s = null;
				            RuntimeException ex = new RuntimeException();
				            try {
				                 if (b)
				                     throw ex;
				                 s = "be";
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
// do-while
public void testBug345305_14() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo1(boolean b) {
				        while (true) {
				            String s = null;
				            try {
				                 do {
				                     s = "be";
				                     if (b)
				                         return;
				                 } while (true);
				            }
				            finally {
				                s.length(); // don't complain here
				                s = null;
				            }
				        }
				    }
				    void foo2(boolean b) {
				        while (true) {
				            String s = null;
				            try {
				                 do {
				                     if (b)
				                         continue;
				                     s = "be";
				                     b = !b;
				                 } while (b);
				            }
				            finally {
				                s.length();
				                s = null;
				            }
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 30)
				s.length();
				^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}

// Bug 364326 - [compiler][null] NullPointerException is not found by compiler. FindBugs finds that one
public void testBug364326() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // employs auto-unboxing
	runNegativeNullTest(
		new String[] {
			"NPE_OnBoxing.java",
			"""
				
				public class NPE_OnBoxing
				{
				    private interface IValue
				    {
				        boolean isSomething();
				    }
				
				    private final IValue m_Value;
				
				    public NPE_OnBoxing()
				    {
				        m_Value = null;
				    }
				
				    public boolean isSomething()
				    {
				        return m_Value != null ? m_Value.isSomething() : null;
				    }
				
				    public static void main(final String [] args)
				    {
				        new NPE_OnBoxing().isSomething();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in NPE_OnBoxing.java (at line 18)
				return m_Value != null ? m_Value.isSomething() : null;
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
			----------
			""");
}

// Bug 401088 - [compiler][null] Wrong warning "Redundant null check" inside nested try statement
public void testBug401088() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					private static void occasionallyThrowException() throws Exception {
						throw new Exception();
					}
				
					private static void open() throws Exception {
						occasionallyThrowException();
					}
				
					private static void close() throws Exception {
						occasionallyThrowException();
					}
				
					public static void main(String s[]) {
						Exception exc = null;
						try {
							open();
							// do more things
						}
						catch (Exception e) {
							exc = e;
						}
						finally {
							try {
								close();
							}
							catch (Exception e) {
								if (exc == null) // should not warn on this line
									exc = e;
							}
						}
						if (exc != null)
							System.out.println(exc);
					}
				}
				"""
		},
		"java.lang.Exception");
}
// Bug 401088 - [compiler][null] Wrong warning "Redundant null check" inside nested try statement
public void testBug401088a() {
 runConformTest(
     new String[] {
         "X.java",
         """
			public class X {
			
			   private static void occasionallyThrowException() throws Exception {
			       throw new Exception();
			   }
			
			   private static void open() throws Exception {
			       occasionallyThrowException();
			   }
			
			   private static void close() throws Exception {
			       occasionallyThrowException();
			   }
			
			   public static void main(String s[]) {
			       Exception exc = null;
			       try {
			           open();
			           // do more things
			       }
			       catch (Exception e) {
			           exc = e;
			       }
			       finally {
			           try {
			               close();
			           }
			           catch (Exception e) {
			               if (exc == null) // should not warn on this line
			                   exc = e;
			           }
			           finally { System.out.print(1); }
			       }
			       if (exc != null)
			           System.out.println(exc);
			   }
			}
			"""
     },
     "1java.lang.Exception");
}
// Bug 401092 - [compiler][null] Wrong warning "Redundant null check" in outer catch of nested try
public void test401092() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Date;
				
				public class X {
				
				    private static void occasionallyThrowException() throws Exception {
				        throw new Exception();
				    }
				
				    private static Date createDate() throws Exception {
				        occasionallyThrowException();
				        return new Date();
				    }
				
				    public static void main(String s[]) {
				        Date d = null;
				        try {
				            d = createDate();
				            System.out.println(d.toString());
				            try {
				                occasionallyThrowException();
				            }
				            catch (Exception exc) {
				            }
				        }
				        catch (Exception exc) {
				            if (d != null) // should not warn in this line
				                System.out.println(d.toString());
				        }
				    }
				}
				"""
		});
}
// Bug 401092 - [compiler][null] Wrong warning "Redundant null check" in outer catch of nested try
public void test401092a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Date;
				
				public class X {
				
				    private static void occasionallyThrowException() throws Exception {
				        throw new Exception();
				    }
				
				    private static Date createDate() throws Exception {
				        occasionallyThrowException();
				        return new Date();
				    }
				
				    public static void main(String s[]) {
				        Date d = null;
				        try {
				            d = createDate();
				            System.out.println(d.toString());
				            try {
				                occasionallyThrowException();
				            }
				            catch (Exception exc) {
				            }
				            finally { System.out.println(1); }
				        }
				        catch (Exception exc) {
				            if (d != null) // should not warn in this line
				                System.out.println(d.toString());
				        }
				        finally { System.out.println(2); }
				    }
				}
				"""
		});
}
// Bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
public void testBug402993() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				
					private static void occasionallyThrowException() throws Exception {
						if ((System.currentTimeMillis() & 1L) != 0L)
							throw new Exception();
					}
				
					private static void open() throws Exception {
						occasionallyThrowException();
					}
				
					private static void close() throws Exception {
						occasionallyThrowException();
					}
				
					public static void main(String s[]) {
						Exception exc = null;
						try {
							open();
							// do more things
						}
						catch (Exception e) {
							if (exc == null) // no warning here ??
								;
						}
						finally {
							try {
								close();
							}
							catch (Exception e) {
								if (exc == null) // No warning here ??
									exc = e;
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 23)
				if (exc == null) // no warning here ??
				    ^^^
			Redundant null check: The variable exc can only be null at this location
			----------
			2. ERROR in Test.java (at line 31)
				if (exc == null) // No warning here ??
				    ^^^
			Redundant null check: The variable exc can only be null at this location
			----------
			""");
}
// Bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
// variant with finally block in inner try
public void testBug402993a() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				
					private static void occasionallyThrowException() throws Exception {
						if ((System.currentTimeMillis() & 1L) != 0L)
							throw new Exception();
					}
				
					private static void open() throws Exception {
						occasionallyThrowException();
					}
				
					private static void close() throws Exception {
						occasionallyThrowException();
					}
				
					public static void main(String s[]) {
						Exception exc = null;
						try {
							open();
							// do more things
						}
						catch (Exception e) {
							if (exc == null) // no warning here ??
								;
						}
						finally {
							try {
								close();
							}
							catch (Exception e) {
								if (exc == null) // No warning here ??
									exc = e;
							} finally {
								System.out.print(1);
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 23)
				if (exc == null) // no warning here ??
				    ^^^
			Redundant null check: The variable exc can only be null at this location
			----------
			2. ERROR in Test.java (at line 31)
				if (exc == null) // No warning here ??
				    ^^^
			Redundant null check: The variable exc can only be null at this location
			----------
			""");
}
public void testBug453305() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach loop
	runConformTest(
		new String[] {
			"NullTest.java",
			"""
				import java.util.*;
				public class NullTest {
				    class SomeOtherClass {
				
				        public SomeOtherClass m() {
				            return new SomeOtherClass();
				        }
				
				        public void doSomething() {
				        }
				    }
				
				    public Object m1() {
				        SomeOtherClass result = null;
				        List<Object> list = new ArrayList<Object>();
				        for (Object next : list) {
				            System.out.println(next);
				            boolean bool = false;
				            if (bool) {
				                SomeOtherClass something = new SomeOtherClass();
				                result = something.m();
				            } else {
				                result = new SomeOtherClass();
				            }
				            result.doSomething(); // warning is here
				            break;
				        }
				        return null;
				    }
				}
				"""
		});
}
public void testBug431016() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach loop
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				  void test(Object[] values) {
				    Object first = null;
				    for (Object current : values) {
				        if (first == null) {
				            first = current;
				        }
				
				        if (current.hashCode() > 0) {
				            System.out.println(first.hashCode());
				        }
				
				        System.out.println(first.hashCode());
				      }
				  }
				}
				"""
		});
}
// originally created for documentation purpose, see https://bugs.eclipse.org/453483#c9
public void testBug431016_simplified() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				  void test(Object input, boolean b) {
				    Object o = null;
				    while (true) {
				      if (o == null)
				        o = input;
				      if (b)
				        o.toString();
				      o.toString();
				    }
				  }
				}
				"""
		});
}
public void testBug432109() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics & foreach loop
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.util.Collection;
				public class Test {
				  public void test(Collection <Object> values)
				  {
				      boolean condition = false;
				     \s
				      for(Object value : values)
				      {
				                 \s
				          if(value == null)
				          {
				              if( condition )
				              {
				                  // without this continue statement,\s
				                  // there is no warning below
				                  continue;\s
				              }
				             \s
				              value = getDefaultValue();
				          }
				         \s
				          // IDE complains here about potential null pointer access
				          value.toString();
				      }
				  }
				
				  public String getDefaultValue() { return "<empty>"; }
				}
				"""
		});
}
public void testBug435528_orig() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Test.java",
			"""
				public class Test
				{
				   static final String a = "A";
				
				   static void main(String args[])
				   {
				      String x = null;
				      while (true) {
				         x = Math.random() < 0.5 ? a : "BB";
				         if (a != null) {
				            System.out.println("s2 value: " + x);
				         }
				         if (x.equals("A")) {
				            break;
				         } else {
				            x = null;
				         }
				      }
				   }
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in Test.java (at line 10)
				if (a != null) {
				    ^
			Redundant null check: The field a is a nonnull constant
			----------
			2. WARNING in Test.java (at line 15)
				} else {
			            x = null;
			         }
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""";
	runner.customOptions = getCompilerOptions();
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug435528_notaconstant() {
	runConformTest(
		true/*flush*/,
		new String[] {
			"Test.java",
			"""
				public class Test
				{
				   static String a	;
				
				   static void main(String args[])
				   {
				      String x = null;
				      while (true) {
				         x = Math.random() < 0.5 ? a : "BB";
				         if (a != null) {
				            System.out.println("s2 value: " + x);
				         }
				         if (x.equals("A")) {
				            break;
				         } else {
				            x = null;
				         }
				      }
				   }
				}
				"""
		},
		"""
			----------
			1. WARNING in Test.java (at line 15)
				} else {
			            x = null;
			         }
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally
			----------
			""",
		"",
		"",
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
public void testBug418500() {
	runConformTest(
		new String[] {
			"Test.java",
			"import java.util.*;\n" +
			"public class Test {\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_5 ? "\n" : "  @SuppressWarnings(\"unchecked\")\n" ) +
			"  void method() {\n" +
			"    Map topMap = new HashMap();\n" +
			"    List targets = null;\n" +
			"    \n" +
			"    for (int idx = 1; idx < 100; idx++) {\n" +
			"      String[] targetArray = (String[]) topMap.get(\"a\");\n" +
			"      if (targetArray != null) {\n" +
			"        targets = Arrays.asList(targetArray);\n" +
			"      } else {\n" +
			"        targets = new ArrayList(64);\n" +
			"      }\n" +
			"      if (targets.size() > 0) {\n" +
			"        topMap.put(\"b\", targets.toArray(new String[1]));\n" +
			"      } else {\n" +
			"        topMap.remove(\"b\");\n" +
			"      }\n" +
			"\n" +
			"      // BUG - this statement causes null analysis to\n" +
			"      // report that at the targets.size() statement above\n" +
			"      // targets must be null. Commenting this line eliminates the error.\n" +
			"      targets = null;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		});
}
public void testBug441737() {
	runConformTest(
		new String[] {
			"Bogus.java",
			"""
				public class Bogus {
				    static boolean ok = true;
				    static int count = 0;
				    public static void main(String[] args) {
				        Thing x = new Thing();
				        // if y is left uninitialized here, the warning below disappears
				        Thing y = null;
				        do {
				            y = x;
				            if (ok) {
				                // if this assignment is moved out of the if statement
				                // or commented out, the warning below disappears
				                x = y.resolve();
				            }
				            // a warning about y being potentially null occurs here:
				            x = y.resolve();
				        } while (x != y);
				    }
				
				    static class Thing {
				        public Thing resolve() {
				            return count++ > 2 ? this : new Thing();
				        }
				    }
				}
				"""
		});
}
// fixed in 3.6.2, likely via bug 332637.
public void testBug195638_comment3() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				import java.sql.Connection;
				import java.sql.SQLException;
				public class Test {
				  void m() throws SQLException
				  {
				    Connection conn = null;
				    try
				    {
				      conn = createConnection();
				
				      for (; ; )
				      {
				        throwSomething();
				      }
				    }
				    catch (MyException e)
				    {
				      conn.rollback(); //The variable can never be null here...
				    }
				  }
				
				  private void throwSomething() throws MyException
				  {
				    throw new MyException();
				  }
				
				  class MyException extends Exception
				  {
				
				  }
				
				  private Connection createConnection()
				  {
				    return null;
				  }
				}
				"""
		});
}
public void testBug195638_comment6() {
	runNegativeNullTest(
		new String[] {
			"CanOnlyBeNullShouldBeMayBeNull.java",
			"""
				public class CanOnlyBeNullShouldBeMayBeNull {
				
					private void method() {
						String tblVarRpl = null;
						while (true) {
							boolean isOpenVariableMortageRateProduct = true;
							boolean tblVarRplAllElementAddedIndicator = false;
							if (isOpenVariableMortageRateProduct) {
								if (tblVarRplAllElementAddedIndicator == false)
									tblVarRpl = "";
								tblVarRpl.substring(1);	//Can only be null???
								return;\s
							}
						}
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in CanOnlyBeNullShouldBeMayBeNull.java (at line 3)
				private void method() {
				             ^^^^^^^^
			The method method() from the type CanOnlyBeNullShouldBeMayBeNull is never used locally
			----------
			2. ERROR in CanOnlyBeNullShouldBeMayBeNull.java (at line 11)
				tblVarRpl.substring(1);	//Can only be null???
				^^^^^^^^^
			Potential null pointer access: The variable tblVarRpl may be null at this location
			----------
			""");
}
public void testBug195638_comment14() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    private void test() {\n" +
			"        boolean x = true;\n" +
			"        Object o = null;\n" +
			"        \n" +
			"        for (;;) {\n" +
			"            if (x) o = new Object();\n" +
			"            \n" +
			"            o.toString(); // warning here\n" + // bug was: Null pointer access: The variable o can only be null at this location
			"            \n" +
			"            o = null;\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"""
			----------
			1. WARNING in Test.java (at line 2)
				private void test() {
				             ^^^^^^
			The method test() from the type Test is never used locally
			----------
			2. ERROR in Test.java (at line 9)
				o.toString(); // warning here
				^
			Potential null pointer access: The variable o may be null at this location
			----------
			""");
}
public void testBug195638_comment19() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    public void testIt() {
				      Object aRole = null;
				      for (;;) {
				        aRole = new Object();
				        if (aRole.toString() == null) {
				          aRole = getObject(); // changing to "new Object()" makes warning disappear.
				        }
				        aRole.toString();
				        // above line gets: "Null pointer access: The variable aRole can only be null at this location"
				        break;
				      }
				    }
				    private Object getObject() {
				      return new Object();
				    }
				}
				"""
		});
}
public void testBug454031() {
	runNegativeNullTest(
		new String[] {
			"xy/Try.java",
			"""
				package xy;
				
				public class Try {
				    public static void main(String[] args) {
				        foo(new Node());
				    }
				    static void foo(Node n) {
				        Node selectedNode= n;
				        if (selectedNode == null) {
				            return;
				        }
				        while (selectedNode != null && !(selectedNode instanceof Cloneable)) {
				            selectedNode= selectedNode.getParent();
				        }
				        if (selectedNode == null) { //wrong problem: Null comparison always yields false: The variable selectedNode cannot be null at this location
				            // wrong problem: dead code
				            System.out.println(selectedNode.hashCode());
				        }
				    }
				}
				
				class Node {
				    Node getParent() {
				        return null;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in xy\\Try.java (at line 17)
				System.out.println(selectedNode.hashCode());
				                   ^^^^^^^^^^^^
			Null pointer access: The variable selectedNode can only be null at this location
			----------
			""");
}
// switch with fall-through nested in for:
public void testBug451660() {
	runNegativeNullTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        public static void main(String[] args)
				    {
				        String s = null;
				        for(; true;) // ok with "while(true)"
				        {
				            switch(0)
				            {
				            default:
				                s = "Hello!";
				            case 1:
				                System.out.println(s.toString());
				            }
				            return;
				        }
				    }
				
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				System.out.println(s.toString());
				                   ^
			Potential null pointer access: The variable s may be null at this location
			----------
			""");
}
public void testBug486912KnownNullInLoop() {
	runNegativeNullTest(
		new String[] {
			"test/KnownNullInLoop.java",
			"package test;\n" +
			"\n" +
			"public class KnownNullInLoop {\n" +
			"	public void testDoWhile() {\n" +
			"		Object o1 = null;\n" +
			"		do {\n" +
			"			o1.hashCode(); // ERROR1: known null, but no problem reported.\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak() {\n" +
			"		Object o1 = null;\n" +
			"		while (true) {\n" +
			"			o1.hashCode(); // ERROR2: known null, but no problem reported.\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. ERROR in test\\KnownNullInLoop.java (at line 7)
				o1.hashCode(); // ERROR1: known null, but no problem reported.
				^^
			Null pointer access: The variable o1 can only be null at this location
			----------
			2. ERROR in test\\KnownNullInLoop.java (at line 14)
				o1.hashCode(); // ERROR2: known null, but no problem reported.
				^^
			Null pointer access: The variable o1 can only be null at this location
			----------
			"""
	);
}
public void testBug486912PotNullInLoop_orig() {
	runNegativeNullTest(
		new String[] {
			"test/PotNullInLoop.java",
			"package test;\n" +
			"\n" +
			"public class PotNullInLoop {\n" +
			"	boolean b;\n" +
			"\n" +
			"	public void testDoWhile1() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" + // actually: def nn
			"		Object potNull = b ? o1 : o1;\n" +	  // actually: def n
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		do {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 1: pot null, but nothing reported\n" +
			"			ponNullOrNonNull.hashCode(); // ERROR 2: pot null, but nothing reported\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 3 : pot null, but nothing reported\n" +
			"			ponNullOrNonNull.hashCode(); // ERROR 4: pot null, but nothing reported\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testWhile() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"			ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testFor() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"			ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testForEach() {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o2;\n" +
			"		Object potNull = b ? o1 : o1;\n" +
			"		Object ponNullOrNonNull = b ? potNull : potNonNull;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"			ponNullOrNonNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. ERROR in test\\PotNullInLoop.java (at line 14)
				potNull.hashCode(); // ERROR 1: pot null, but nothing reported
				^^^^^^^
			Null pointer access: The variable potNull can only be null at this location
			----------
			2. ERROR in test\\PotNullInLoop.java (at line 15)
				ponNullOrNonNull.hashCode(); // ERROR 2: pot null, but nothing reported
				^^^^^^^^^^^^^^^^
			Potential null pointer access: The variable ponNullOrNonNull may be null at this location
			----------
			3. ERROR in test\\PotNullInLoop.java (at line 27)
				potNull.hashCode(); // ERROR 3 : pot null, but nothing reported
				^^^^^^^
			Null pointer access: The variable potNull can only be null at this location
			----------
			4. ERROR in test\\PotNullInLoop.java (at line 28)
				ponNullOrNonNull.hashCode(); // ERROR 4: pot null, but nothing reported
				^^^^^^^^^^^^^^^^
			Potential null pointer access: The variable ponNullOrNonNull may be null at this location
			----------
			5. ERROR in test\\PotNullInLoop.java (at line 41)
				potNull.hashCode(); // OK: pot null, is reported
				^^^^^^^
			Null pointer access: The variable potNull can only be null at this location
			----------
			6. ERROR in test\\PotNullInLoop.java (at line 42)
				ponNullOrNonNull.hashCode(); // OK: pot null, is reported
				^^^^^^^^^^^^^^^^
			Potential null pointer access: The variable ponNullOrNonNull may be null at this location
			----------
			7. ERROR in test\\PotNullInLoop.java (at line 54)
				potNull.hashCode(); // OK: pot null, is reported
				^^^^^^^
			Null pointer access: The variable potNull can only be null at this location
			----------
			8. ERROR in test\\PotNullInLoop.java (at line 55)
				ponNullOrNonNull.hashCode(); // OK: pot null, is reported
				^^^^^^^^^^^^^^^^
			Potential null pointer access: The variable ponNullOrNonNull may be null at this location
			----------
			9. ERROR in test\\PotNullInLoop.java (at line 67)
				potNull.hashCode(); // OK: pot null, is reported
				^^^^^^^
			Null pointer access: The variable potNull can only be null at this location
			----------
			10. ERROR in test\\PotNullInLoop.java (at line 68)
				ponNullOrNonNull.hashCode(); // OK: pot null, is reported
				^^^^^^^^^^^^^^^^
			Potential null pointer access: The variable ponNullOrNonNull may be null at this location
			----------
			"""
	);
}
// variant of testBug486912PotNullInLoop_orig spiced up with potentiality from an 'unknown' o0:
public void testBug486912PotNullInLoop() {
	runNegativeNullTest(
		new String[] {
			"test/PotNullInLoop.java",
			"package test;\n" +
			"\n" +
			"public class PotNullInLoop {\n" +
			"	boolean b;\n" +
			"\n" +
			"	public void testDoWhile1(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		do {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 1: pot null, but nothing reported\n" +
			"		} while (false);\n" +
			"	}\n" +
			"\n" +
			"	public void testWhileWithBreak(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // ERROR 3 : pot null, but nothing reported\n" +
			"			break;\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testWhile(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		while (b) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testFor(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public void testForEach(Object o0) {\n" +
			"		Object o1 = null;\n" +
			"		Object o2 = new Object();\n" +
			"		Object potNonNull = b ? o2 : o0;\n" +
			"		Object potNull = b ? o1 : o0;\n" +
			"		for (int i = 0; i < 1; i++) {\n" +
			"			potNonNull.hashCode(); // OK\n" +
			"			potNull.hashCode(); // OK: pot null, is reported\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"}\n" +
			"",
		},
		"""
			----------
			1. ERROR in test\\PotNullInLoop.java (at line 13)
				potNull.hashCode(); // ERROR 1: pot null, but nothing reported
				^^^^^^^
			Potential null pointer access: The variable potNull may be null at this location
			----------
			2. ERROR in test\\PotNullInLoop.java (at line 24)
				potNull.hashCode(); // ERROR 3 : pot null, but nothing reported
				^^^^^^^
			Potential null pointer access: The variable potNull may be null at this location
			----------
			3. ERROR in test\\PotNullInLoop.java (at line 36)
				potNull.hashCode(); // OK: pot null, is reported
				^^^^^^^
			Potential null pointer access: The variable potNull may be null at this location
			----------
			4. ERROR in test\\PotNullInLoop.java (at line 47)
				potNull.hashCode(); // OK: pot null, is reported
				^^^^^^^
			Potential null pointer access: The variable potNull may be null at this location
			----------
			5. ERROR in test\\PotNullInLoop.java (at line 58)
				potNull.hashCode(); // OK: pot null, is reported
				^^^^^^^
			Potential null pointer access: The variable potNull may be null at this location
			----------
			"""
	);
}
public void testBug447695() {
	runConformTest(
		new String[] {
		"test/Test447695.java",
			"""
				package test;
				
				public class Test447695 {
					public static void f() {
						int[] array = null;
						(array = new int[1])[0] = 42;
					}
					public static int g() {
						int[] array = null;
						return (array = new int[1])[0];
					}
				}
				"""
		}
	);
}
public void testBug447695b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
					void test(String[] ss) {
						List<String> strings = null;
						for (String s : (strings = Arrays.asList(ss)))
							System.out.println(s);
					}
				}
				"""
		});
}
public void testBug447695c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"""
				package test;
				
				public class Test447695 {
					void f() {
						Integer l1 = null;
						Integer l2 = null;
						int b = (l1 = new Integer(2)) + (l2 = new Integer(1));
					}
				}
				"""
		}
	);
}
public void testBug447695d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses reference expression
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"""
				package test;
				
				import java.util.function.Supplier;
				
				public class Test447695 {
					void f() {
						String s = null;
						Supplier<String> l = (s = "")::toString;
					}
				}
				"""
		}
	);
}
public void testBug447695e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"""
				package test;
				
				public class Test447695 {
					void f() {
						Integer i = null;
						int j = -(i = new Integer(1));
						Boolean b1 = null;
						boolean b = !(b1 = new Boolean(false));
					}
				}
				"""
		}
	);
}
public void testBug447695f() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	runConformTest(
		new String[] {
			"test/Test447695.java",
			"""
				package test;
				
				public class Test447695 {
					void f() {
						int i = 0;
						Integer i1 = null;
						Integer i2 = null;
						Integer i3 = null;
						int j = (i1 = new Integer(1))\s
								+ (i2 = new Integer(1))\s
								+ i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i\s
								+ (i3 = new Integer(2)) + i;
					}
				}
				"""
		}
	);
}
public void testBug447695g() {
	runNegativeNullTest(
		new String[] {
			"test/Test447695.java",
			"""
				package test;
				
				class X {
					int i;
				}
				
				public class Test447695 {
					void f() {
						X x1 = null;
						X x2 = null;
						X x3 = null;
						X x4 = null;
						X x5 = null;
						X x6 = null;
						X x7 = null;
						X x8 = null;
						X x9 = null;
						X x10 = null;
						X x11 = null;
						x1.i = 1; // error 1 expected
						x2.i += 1; // error 2 expected
						(x3).i = 1; // error 3 expected
						(x4).i += 1; // error 4 expected
						(x5 = new X()).i = (x6 = new X()).i;
						(x7 = new X()).i += (x8 = new X()).i;
						int i1 = x9.i; // error 5 expected
						int i2 = (x10).i; // error 6 expected
						int i3 = (x11 = new X()).i;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in test\\Test447695.java (at line 20)
				x1.i = 1; // error 1 expected
				^^
			Null pointer access: The variable x1 can only be null at this location
			----------
			2. ERROR in test\\Test447695.java (at line 21)
				x2.i += 1; // error 2 expected
				^^
			Null pointer access: The variable x2 can only be null at this location
			----------
			3. ERROR in test\\Test447695.java (at line 22)
				(x3).i = 1; // error 3 expected
				^^^^
			Null pointer access: The variable x3 can only be null at this location
			----------
			4. ERROR in test\\Test447695.java (at line 23)
				(x4).i += 1; // error 4 expected
				^^^^
			Null pointer access: The variable x4 can only be null at this location
			----------
			5. ERROR in test\\Test447695.java (at line 26)
				int i1 = x9.i; // error 5 expected
				         ^^
			Null pointer access: The variable x9 can only be null at this location
			----------
			6. ERROR in test\\Test447695.java (at line 27)
				int i2 = (x10).i; // error 6 expected
				         ^^^^^
			Null pointer access: The variable x10 can only be null at this location
			----------
			"""
	);
}
public void testBug509188() {
	runConformTest(
		new String[] {
			"test/Bug509188.java",
			"package test;\n" +
			"\n" +
			"public class Bug509188 {\n" +
			"	public static class QuinamidCell {\n" +
			"		public QuinamidCell next;\n" +
			"	}\n" +
			"\n" +
			"	public static void drawBoardElements() {\n" +
			"		Object _a, _b, _c, _d, _e, _f, _g, _h, _i, _j, _k, _l, _m, _n, _o, _p, _q, _r, _s, _t, _u, _v, _w, _x, _y, _z,\n" +
			"				_a1, _b1, _c1, _d1, _e1, _f1, _g1, _h1, _i1, _j1, _k1, _l1, _m1, _n1, _o1, _p1, _q1, _r1, _s1, _t1, _u1,\n" +
			"				_v1, _w1, _x1, _y1, _z_1, _a2, _b2, _c2, _d2, _e2, _f2, _g2, _h2, _i2, _j2, _k2;\n" +
			"\n" +
			"		QuinamidCell hitCell = null;\n" +
			"\n" +
			"		int level = 0; while (level < 1) {\n" +
			"			for (QuinamidCell c = new QuinamidCell(); c != null; c = c.next) {\n" +
			"				hitCell = c;\n" +
			"			} level++;\n" +
			"		}\n" +
			"		if (hitCell != null) {\n" +
			"			System.out.println(\"not dead\");\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		drawBoardElements();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		"not dead"
	);
}
public void testBug536408() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses auto unboxing
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        Long s1 = null;
				        long t = 0;
				        t += s1;
						 Long s2 = t > 0 ? 1l : null;
						 t += s2;
				    }
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				t += s1;
				     ^^
			Null pointer access: This expression of type Long is null but requires auto-unboxing
			----------
			2. ERROR in X.java (at line 7)
				t += s2;
				     ^^
			Potential null pointer access: This expression of type Long may be null but requires auto-unboxing
			----------
			""";
	runner.javacTestOptions = JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug542707_1() {
	if (!checkPreviewAllowed()) return; // switch expression
	Runner runner = new Runner();
	runner.customOptions = new HashMap<>();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
	runner.testFiles = new String[] {
		"X.java",
		"""
			public class X {
				void test(int i) {
					String s = switch (i) {
						case 1 -> "one";
						default -> null;
					};
					System.out.println(s.toLowerCase());
				}
			}
			"""
	};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 7)
					System.out.println(s.toLowerCase());
					                   ^
				Potential null pointer access: The variable s may be null at this location
				----------
				""";
	runner.runNegativeTest();
}
public void testBug544872() {
	runNegativeNullTest(
		new String[] {
			"Test.java",
			"public class Test {\n" +
			"    static void f(String string) {\n" +
			"        if (string != null)\n" +
			"            string.hashCode();\n" +
			"        synchronized (string) {\n" +
			"            string.hashCode();\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			""
		},
		"""
			----------
			1. ERROR in Test.java (at line 5)
				synchronized (string) {
				              ^^^^^^
			Potential null pointer access: The variable string may be null at this location
			----------
			"""
	);
}
public void testBug551012() {
	runNegativeNullTest(
		new String[] {
			"NullConstants.java",
			"""
				public class NullConstants {
					protected static final String FOO = null;
				
					protected String foo = FOO;
				
					protected static final String BAR = "";
				
					protected String bar = BAR;
				
					public boolean notAProblemButWhyNot() {
						return FOO == null ? foo != null : !FOO.equals(foo);
					}
				
					public boolean alsoNotAProblemButThisWillAlwaysNPE() {
						return FOO != null ? foo != null : !FOO.equals(foo);
					}
				
					public boolean aProblemButHowToAvoid() {
						return BAR == null ? bar != null : !BAR.equals(bar);
					}
				
					public boolean wrongpProblemMessage() {
						return BAR != null ? !BAR.equals(bar) : bar != null;
					}
				
					public boolean howAboutThis() {
						return bar == null ? BAR != null : bar.equals(BAR);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in NullConstants.java (at line 19)
				return BAR == null ? bar != null : !BAR.equals(bar);
				       ^^^
			Null comparison always yields false: The field BAR is a nonnull constant
			----------
			2. ERROR in NullConstants.java (at line 23)
				return BAR != null ? !BAR.equals(bar) : bar != null;
				       ^^^
			Redundant null check: The field BAR is a nonnull constant
			----------
			3. ERROR in NullConstants.java (at line 27)
				return bar == null ? BAR != null : bar.equals(BAR);
				                     ^^^
			Redundant null check: The field BAR is a nonnull constant
			----------
			""");
}
public void testBug561280() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics
	runConformTest(
		new String[] {
			"test/Test.java",
			"""
				package test;
				
				import java.util.List;
				import java.util.Set;
				
				public class Test
				{
				  protected static final String ERROR_TYPE = "error";
				  protected static final String OBJECT_TYPE = "object";
				  protected static final String UNKNOWN_FEATURE_TYPE = "unknownFeature";
				  protected static final String DOCUMENT_ROOT_TYPE = "documentRoot";
				
				  protected final static String TYPE_ATTRIB = "";
				  protected final static String NIL_ATTRIB = "";
				  protected final static String SCHEMA_LOCATION_ATTRIB = "";
				  protected final static String NO_NAMESPACE_SCHEMA_LOCATION_ATTRIB = "";
				
				  protected final static boolean DEBUG_DEMANDED_PACKAGES = false;
				
				
				  protected Object xmlResource;
				  protected Object helper;
				  protected Object elements;
				  protected Object objects;
				  protected Object types;
				  protected Object mixedTargets;
				  protected Object prefixesToFactories;
				  protected Object urisToLocations;
				  protected Object externalURIToLocations;
				  protected boolean processSchemaLocations;
				  protected Object extent;
				  protected Object deferredExtent;
				  protected Object resourceSet;
				  protected Object packageRegistry;
				  protected Object resourceURI;
				  protected boolean resolve;
				  protected boolean oldStyleProxyURIs;
				  protected boolean disableNotify;
				  protected StringBuffer text;
				  protected boolean isIDREF;
				  protected boolean isSimpleFeature;
				  protected Object sameDocumentProxies;
				  protected Object[] identifiers;
				  protected int[] positions;
				  protected static final int ARRAY_SIZE = 64;
				  protected static final int REFERENCE_THRESHOLD = 5;
				  protected int capacity;
				  protected Set<String> notFeatures;
				  protected String idAttribute;
				  protected String hrefAttribute;
				  protected Object xmlMap;
				  protected Object extendedMetaData;
				  protected Object anyType;
				  protected Object anySimpleType;
				  protected boolean recordUnknownFeature;
				  protected boolean useNewMethods;
				  protected boolean recordAnyTypeNSDecls;
				  protected Object eObjectToExtensionMap;
				  protected Object contextFeature;
				  protected Object xmlSchemaTypePackage = null;
				  protected boolean deferIDREFResolution;
				  protected boolean processAnyXML;
				  protected Object ecoreBuilder;
				  protected boolean isRoot;
				  protected Object locator;
				  protected Object attribs;
				  protected boolean useConfigurationCache;
				  protected boolean needsPushContext;
				  protected Object resourceEntityHandler;
				  protected Object uriHandler;
				  protected Object documentRoot;
				  protected boolean usedNullNamespacePackage;
				  protected boolean isNamespaceAware;
				  protected boolean suppressDocumentRoot;
				  protected boolean laxWildcardProcessing;
				
				  protected static void processObjectx(Object object)
				  {
				    if (object instanceof List)
				    {
				      List<?> list = ((List<?>)object);
				      list.size();
				    }
				
				    if (object != null)
				    {
				      object.hashCode();
				    }
				    else
				    {
				      System.err.println("#");
				    }
				  }
				}
				"""
		});
}
public void testBug380786() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses foreach
	runNegativeTest(
		new String[] {
			"PNA.java",
			"""
				public class PNA {
				  void missedPNA(String s) {
				    if (s != null)
				      s = "1,2";
				    final String[] sa = s.split(",");
				    for (final String ss : sa)
				      System.out.println(ss);
				  }
				
				  void detectedPNA(final String ps) {
				    String s = ps;
				    if (s != null)
				      s = "1,2";
				    final String[] sa = s.split(",");
				    for (final String ss : sa)
				      System.out.println(ss);
				  }
				 \s
				}
				"""
		},
		"""
			----------
			1. ERROR in PNA.java (at line 5)
				final String[] sa = s.split(",");
				                    ^
			Potential null pointer access: The variable s may be null at this location
			----------
			2. ERROR in PNA.java (at line 14)
				final String[] sa = s.split(",");
				                    ^
			Potential null pointer access: The variable s may be null at this location
			----------
			"""
			);
}
public void testGH1642_a() {
	runConformTest(
		new String[] {
			"Bug.java",
			"""
			public class Bug {
				void m(Object o) {
					if (o == null)
						System.out.println();
					if (o instanceof String || o instanceof Integer)
						o.toString();
				}
			}
			"""});
}
public void testGH1667() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses foreach
	runConformTest(
		new String[] {
			"Foo.java",
			"""
			class EPD {
				public String getModel() { return null; }
			}
			class IPS {
				public EPD getSupplier() { return null; }
			}
			public class Foo {
				void m(String packageName, IPS[] packages) {
					String base = null;
					for (IPS spec : packages) {
						EPD desc = spec.getSupplier();
						if (desc != null) {
							base = desc.getModel();
						}
						break;
					}
					if (base != null) {
						System.out.println();
					}
				}
			}
			"""
		});
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1461
public void testGH1461() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Objects;
				public class X {
					public void test() {
						String name = null;
						if (Objects.isNull(name)) {
							System.out.println("Name is null");
							return;
						}
						System.out.println(name.substring(0, 4));
					}
				}
				"""
			},
		"""
			----------
			1. WARNING in X.java (at line 9)
				System.out.println(name.substring(0, 4));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1461
public void testGH1461SuppressWarnings() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return;
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Objects;
				@SuppressWarnings("unused")
				public class X {
					public void test() {
						String name = null;
						if (Objects.isNull(name)) {
							System.out.println("Name is null");
							return;
						}
						System.out.println(name.substring(0, 4));
					}
				}
				"""
			},
		"");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1461
public void testGH1461_a() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Objects;
				public class X {
					public void test() {
						String name = "name";
						if (Objects.nonNull(name)) {
							System.out.println("Name is null");
							return;
						}
						System.out.println(name.substring(0, 4));
					}
				}
				"""
			},
		"""
			----------
			1. WARNING in X.java (at line 9)
				System.out.println(name.substring(0, 4));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1461
public void testGH1461_b() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Objects;
				public class X {
					public void test() {
						String name = null;
						if (!Objects.nonNull(name)) {
							System.out.println("Name is null");
							return;
						}
						System.out.println(name.substring(0, 4));
					}
				}
				"""
			},
		"""
			----------
			1. WARNING in X.java (at line 9)
				System.out.println(name.substring(0, 4));
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1461
public void testGH1461_c() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String... args) {
						Foo foo = new Foo();
						if (Bar.class.isInstance(foo)) {
							return;
						}
						System.out.println("Hello, world!");
					}
				}
				class Foo{}
				class Bar{}
				"""
			},
		"""
			""");
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1461
public void testGH1461_d() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String... args) {
						Foo foo = null;
						if (!Bar.class.isInstance(foo)) {
							return;
						}
						System.out.println("Hello, world!");
					}
				}
				class Foo{}
				class Bar{}
				"""
			},
		"""
			----------
			1. WARNING in X.java (at line 7)
				System.out.println("Hello, world!");
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
public void testGH1755() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
			import java.util.function.Supplier;

			public class X {
				public static void main(String[] args) {
					String a = "Hello";
					if (((Supplier<String>) () -> a) instanceof Supplier)
						System.out.print("yes");
					if (((Supplier<String>) () -> a) != null)
						System.out.print("yes");
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 8)
			if (((Supplier<String>) () -> a) != null)
			    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Redundant null check: this expression cannot be null
		----------
		""");
}
}