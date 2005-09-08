/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class NullReferenceTest extends AbstractRegressionTest {

	public NullReferenceTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	// Only the highest compliance level is run; add the VM argument
	// -Dcompliance=1.4 (for example) to lower it if needed
	static {
//		TESTS_NAMES = new String[] { "test011" };
//	 	TESTS_NUMBERS = new int[] { 2 };   
//		TESTS_RANGE = new int[] { 231, 240 }; 
	}
	public static Test suite() {
		return buildTestSuite(testClass());
	}
	
	public static Class testClass() {
		return NullReferenceTest.class;
	}

	// Augment problem detection settings
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.WARNING);
		return defaultOptions;
	}
	
	// null analysis -- simple case for local
	public void test0001_simple_local() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}
	
	// null analysis -- simple case for field
	// despite the fact that a separate thread may update the field,
	// a comprehensive warning policy could point this case as potentially
	// harmful -- this is not the current design, thow; it takes a 
	// conservative approach and leaves fields out of the analysis altogether
	// TODO (maxime) reset diagnostic once supported
	public void test0002_simple_field() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"  Object o;\n" + 
				"	 void foo() {\n" + 
				"		 o = null;\n" + 
				"	 	 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
		""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			"	o.toString();\n" + 
//			"	^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n"
		);
	}

	// null analysis -- simple case for parameter
	public void test0003_simple_parameter() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 o = null;\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- field
	public void test0004_field_with_method_call() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo() {\n" + 
				"		 o = null;\n" + 
				"		 bar();\n" + // defuses null by side effect
				"		 o.toString();\n" + 
				"	 }\n" + 
				"	 void bar() {\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- field
	public void test0005_field_with_method_call() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 static Object o;\n" + 
				"	 void foo() {\n" + 
				"		 o = null;\n" + 
				"		 bar();\n" + // defuses null by side effect
				"		 o.toString();\n" + 
				"	 }\n" + 
				"	 static void bar() {\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- field
	public void test0006_field_with_method_call() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo() {\n" + 
				"		 o = null;\n" + 
				"		 bar();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"	 static void bar() {\n" + 
				"	 }\n" + 
				"}\n"},
			"" // still ok because the class may hold a pointer to this
		);
	}

	// null analysis -- field
	public void test0007_field_with_method_call() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 static Object o;\n" + 
				"	 void foo() {\n" + 
				"		 o = null;\n" + 
				"		 bar();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"	 void bar() {\n" + 
				"	 }\n" + 
				"}\n"},
			"" // still ok because this may place a static call upon X
		);
	}
	
	// null analysis -- field
	// TODO (maxime) reset diagnostic once supported
	public void test0008_field_with_explicit_this_access() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo() {\n" + 
				"		 o = null;\n" + 
				"		 this.o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			"	this.o.toString();\n" + 
//			"	^^^^^^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n"
		);
	}

	// null analysis -- field
	// TODO (maxime) reset diagnostic once supported
	public void test0009_field_with_explicit_this_access() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo() {\n" + 
				"		 this.o = null;\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			"	o.toString();\n" + 
//			"	^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n"
		);
	}

	// null analysis -- field
	public void test0010_field_of_another_object() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo() {\n" + 
				"	   X other = new X();\n" + 
				"		 other.o = null;\n" + 
				"		 other.o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}
	
	// null analysis -- field
	public void test0011_field_of_another_object() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo() {\n" + 
				"	   X other = this;\n" + 
				"		 o = null;\n" + 
				"		 other.o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- field
	// TODO (maxime) reset diagnostic once supported
	public void test0012_field_of_enclosing_object() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"  public class Y {\n" + 
				"	   void foo() {\n" + 
				"		   X.this.o = null;\n" + 
				"		   X.this.o.toString();\n" + // complain
				"	   }\n" + 
				"  }\n" + 
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 6)\n" + 
//			"	X.this.o.toString();\n" + 
//			"	^^^^^^^^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n"
		);
	}
	
	// null analysis -- fields
	// check that fields that are protected against concurrent access
	// behave as locals when no call to further methods can affect them
	// TODO (maxime) reset diagnostic once supported
	public void test0013_field_synchronized() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"  public synchronized void foo() {\n" + 				
				"		 o = null;\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"  void bar() {/* */}\n" + 				
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			" o.toString();\n" + 
//			"	^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n" 
		);
	}

	// null analysis -- field
	// check that final fields behave as locals despite calls to further 
	// methods
	// TODO (maxime) reset diagnostic once supported
	public void test0014_final_field() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 final Object o = null;\n" + 
				"  public synchronized void foo() {\n" + 				
				"		 bar();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"  void bar() {/* */}\n" + 				
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			"	o.toString();\n" + 
//			"	^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n" 
		);
	}

	// null analysis -- field
	// TODO (maxime) reset diagnostic once supported
	public void test0015_final_field() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 final Object o = null;\n" + 
				"  X () {\n" + 				
				"		 bar();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"  void bar() {/* */}\n" + 				
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			"	o.toString();\n" + 
//			"	^\n" + 
//			"The field o is likely null; it was either set to null or checked for null when last used\n" + 
//			"----------\n" 
		);
	}

	// null analysis -- field
	// TODO (maxime) reset diagnostic once supported
	public void test0016_final_field() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 final Object o = new Object();\n" + 
				"  X () {\n" + 				
				"		 bar();\n" + 
				"		 if (o == null) { /* empty */ }\n" + 
				"	 }\n" + 
				"  void bar() {/* */}\n" + 				
				"}\n"},
			""
//			"----------\n" + 
//			"1. WARNING in X.java (at line 5)\n" + 
//			"	if (o == null) { /* empty */ }\n" + 
//			"	    ^\n" + 
//			"The field o is likely non null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
//			"----------\n"
		);
	}

	// null analysis -- parameter
	public void test0017_parameter() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 o.toString();\n" + // quiet: parameters have unknown value 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- conditional expression
	// TODO (maxime) fix
	public void _test0020_conditional_expression() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = true ? null : null;\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- conditional expression
	// TODO (maxime) fix
	public void _test0021_conditional_expression() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = true ? null : new Object();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- conditional expression
	public void test0022_conditional_expression() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = false ? null : new Object();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- conditional expression
	// TODO (maxime) fix
	public void _test0023_conditional_expression() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = (1 == 1) ? null : new Object();\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- autoboxing
	// TODO (maxime) fix
	public void _test0030_autoboxing_compound_assignment() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Integer i = null;\n" +
					"    i += 1;\n" + 
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	i += 1;\n" + 
				"	^\n" + 
				"The variable i can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- autoboxing
	// TODO (maxime) fix
	public void _test0031_autoboxing_increment_operator() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Integer i = null;\n" +
					"    i++;\n" + // complain: this is null
					"    ++i;\n" + // quiet (because previous step guards it)
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	i++;\n" + 
				"	^\n" + 
				"The variable i can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- autoboxing
	public void test0032_autoboxing_literal() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Integer i = 0;\n" +
					"    if (i == null) {};\n" + // complain: this is non null
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	if (i == null) {};\n" + 
				"	    ^\n" + 
				"The variable i cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- autoboxing
	// TODO (maxime) fix
	public void _test0033_autoboxing_literal() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Integer i = null;\n" +
					"    System.out.println(i + 4);\n" + // complain: this is null
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	System.out.println(i + 4);\n" + 
				"	                   ^\n" + 
				"The variable i can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- autoboxing
	// origin: AssignmentTest#test020
	public void test034_autoboxing() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 int i = 0;\n" +
				"    boolean b = i < 10;\n" + 
				"	 }\n" + 
				"}\n",
			},
		"");
	}

	// null analysis -- array
	public void test0041_array() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 public static void main(String args[]) {\n" + 
				"		 args[0] = null;\n" +
				"    if (args[0] == null) {};\n" + 
				     // quiet: we don't keep track of all array elements
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- array
	public void test0042_array() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 public static void main(String args[]) {\n" + 
				"		 args = null;\n" +
				"    args[0].toString();\n" + // complain: args is null
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	args[0].toString();\n" + 
			"	^^^^\n" + 
			"The variable args can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- type reference
	public void test0051_type_reference() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 public static void main(String args[]) {\n" + 
				"		 Class c = java.lang.Object.class;\n" +
				"    if (c == null) {};\n" +
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	if (c == null) {};\n" + 
			"	    ^\n" + 
			"The variable c cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- method call
	public void test0061_method_call_guard() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"    if (o == null) {};\n" + // quiet: we don't know anything
				"    o.toString();\n" +      // guards o from being null
				"    if (o == null) {};\n" + // complain
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	if (o == null) {};\n" + 
			"	    ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}	
	
	// null analysis - method call
	public void test0062_method_call_isolation() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (bar(o = null)) {\n" + 
				"		   if (o == null) {/* empty */}\n" + // complain 
				"		 }\n" + 
				"	 }\n" + 
				"	 boolean bar(Object o) {\n" + 
				"		 return true;\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	if (o == null) {/* empty */}\n" + 
			"	    ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}		
	
	// null analysis - method call
	public void test0063_method_call_isolation() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (bar(o == null ? new Object() : o)) {\n" + 
				"		   if (o == null) {/* empty */}\n" + // quiet 
				"		 }\n" + 
				"	 }\n" + 
				"	 boolean bar(Object o) {\n" + 
				"		 return true;\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}		
	
	// null analysis - method call
	public void test0064_method_call_isolation() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (bar(o = new Object())) {\n" + 
				"		   if (o == null) {/* empty */}\n" + // complain 
				"		 }\n" + 
				"	 }\n" + 
				"	 boolean bar(Object o) {\n" + 
				"		 return true;\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	if (o == null) {/* empty */}\n" + 
			"	    ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}		

	// null analysis - method call
	public void test0065_method_call_invocation_target() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 (o = new Object()).toString();\n" + // quiet 
				"	 }\n" + 
				"}\n"},
			""
		);
	}		

	// null analysis - method call
	// TODO (maxime) fix
	public void _test0066_method_call_invocation_target() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 (o = null).toString();\n" + // complain 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	(o = null).toString();\n" + 
			"	^^^^^^^^^^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}		
	
	// null analysis -- if/else
	// check that obviously unreachable code does not modify the null
	// status of a local
	// the said code is not marked as unreachable per JLS 14.21 (the rationale
	// being the accommodation for the if (constant_flag_evaluating_to_false)
	// {code...} volontary code exclusion pattern)
	// TODO (maxime) fix
	public void _test0100_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"  public void foo() {\n" + 				
				"		 Object o = null;\n" + 
				"		 if (false) {\n" + 
				"			 o = new Object();\n" + // skipped 
				"		 }\n" + 
				"		 if (true) {\n" + 
				"			 //\n" + 
				"		 }\n" + 
				"		 else {\n" + 
				"			 o = new Object();\n" + // skipped
				"		 }\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 13)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"  
		);
	}

	// null analysis - if/else
	public void test0101_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 if (o != null) {\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	if (o != null) {\n" + 
			"	    ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis - if/else
	public void test0102_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) throws Exception {\n" + 
				"		 if (o == null) {\n" + 
				"			 throw new Exception();\n" + 
				"		 }\n" + 
				"		 if (o != null) {\n" + // only get there if o non null
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	if (o != null) {\n" + 
			"	    ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis - if/else
	public void test0103_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (o == null) {\n" + 
				"			 return;\n" + 
				"		 }\n" + 
				"		 if (o != null) {\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	if (o != null) {\n" + 
			"	    ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis - if/else
	public void test0104_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (o == null) {\n" + 
				"		   o.toString();\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis - if/else
	public void test0105_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (o == null) {\n" + 
				"			 // do nothing\n" + 
				"		 }\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis - if/else
	public void test0106_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (o.toString().equals(\"\")) {\n" + 
				"		   if (o == null) {\n" + // complain: could not get here 
				"			   // do nothing\n" + 
				"		   }\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	if (o == null) {\n" + 
			"	    ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis - if/else
	public void test0107_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 if (o ==  null) {\n" + 
				"			 System.exit(0);\n" + 
				"		 }\n" + 
				"		 if (o == null) {\n" + 
				  // quiet 
				  // a direct call to System.exit() can be recognized as such; yet,
				  // a lot of other methods may have the same property (aka calling
				  // System.exit() themselves.)
				"		   // do nothing\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}		

	// null analysis -- while
	// TODO (maxime) fix
	public void _test0111_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (o.toString() != null) {/* */}\n" +
				      // complain: NPE
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	while (o.toString() != null) {/* */}\n" + 
			"	       ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"  
		);
	}

	// null analysis -- while
	// TODO (maxime) fix
	public void _test0112_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (o != null) {/* */}\n" + 
				  // complain: get o null first time and forever
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	while (o != null) {/* */}\n" + 
			"	       ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- while
	public void test0113_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (o == null) {\n" + 
				      // quiet: first iteration is sure to find o null, 
				      // but other iterations may change it 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0114_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (o == null) {\n" + 
				     // quiet: first iteration is sure to find o null, 
				     // but other iterations may change it 
				"		   if (System.currentTimeMillis() > 10L) {\n" + 
				"		     o = new Object();\n" + 
				"		   }\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	// TODO (maxime) fix
	public void _test0115_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean bar() {\n" + 
				"		 return true;\n" + 
				"	 }\n" + 
				"	 void foo(Object o) {\n" + 
				"		 while (bar() && o == null) {\n" + 
				"		   o.toString();\n" + // complain: NPE
				"		   o = new Object();\n" +
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- while
	// TODO (maxime) fix
	public void _test0116_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo(Object o) {\n" + 
				"	   o = null;\n" +
				"		 while (dummy || o != null) { /* */ }\n" + // o can only be null 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	while (dummy || o != null) {\n" + 
			"	                    ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- while
	// TODO (maxime) fix
	public void _test0117_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (dummy) {\n" +
				"		   o.toString();\n" +  // complain: NPE on first iteration
				"		   o = new Object();\n" +
				"		 }\n" +
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"  
		);
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
	// TODO (maxime) fix
	public void _test0118_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null,\n" + 
				"		        u = new Object(),\n" + 
				"		        v = new Object();\n" + 
				"		 while (o == null) {\n" +
				"		   if (v == null) {\n" +
				"		     o = new Object();\n" +
				"		   };\n" +
				"		   if (u == null) {\n" +
				"		     v = null;\n" +
				"		   };\n" +
				"		   u = null;\n" +
				"		 }\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0119_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (dummy || (o = new Object()).equals(o)) {\n" +
				"		   o.toString();\n" +
				"		 }\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0120_while_nested() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (dummy) {\n" + 
				"		   while (o != null) {\n" + 
				"		     o.toString();\n" + 
				"		   }\n" + 
				"		   if (System.currentTimeMillis() > 10L) {\n" + 
				"		     o = new Object();\n" + 
				"		   }\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	// TODO (maxime) fix
	public void _test0121_while_nested() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null,\n" + 
				"		        u = new Object(),\n" + 
				"		        v = new Object();\n" + 
				"	   while (o == null) {\n" + 
				"		   if (v == null) {\n" +
				"		     o = new Object();\n" +
				"		   };\n" +
				"		   while (o == null) {\n" +
				"		     if (u == null) {\n" +
				"		       v = null;\n" +
				"		     };\n" +
				"  		   u = null;\n" +
				"		   }\n" +
				"		 }\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0122_while_if_nested() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy, other;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (dummy) {\n" +   
				"		   if (other) {\n" + 
				"		     o.toString();\n" +    
				"		   }\n" + 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0123_while_unknown_field() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object o;\n" + 
				"	 void foo(boolean dummy) {\n" + 
				"		 while (dummy) {\n" +
				"		   o = null;\n" +
				"		 }\n" +
				"		 o.toString();\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0124_while_unknown_parameter() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo(Object o) {\n" + 
				"		 while (dummy) {\n" +
				"		   o = null;\n" +
				"		 }\n" +
				"		 o.toString();\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0125_while_unknown_if_else() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" +
				"		 if (dummy) {\n" +
				"		   o = new Object();\n" +
				"		 }\n" +
				"		 while (dummy) {\n" + 
					// limit of the analysis: we do not correlate if and while conditions
				"		   if (o == null) {/* */}\n" + 
				"		 }\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- while
	public void test0126_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (dummy) {\n" + 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}
	
	// null analysis -- while
	// TODO (maxime) fix
	public void _test0127_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (dummy) { /* */ }\n" + // doesn't affect o
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n" 
		);
	}
	
	// null analysis -- while
	// origin AssignmentTest.testO22
	public void test0128_while_try() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean bool() { return true; }\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 while (bool()) {\n" + 
				"			 try {\n" + 
				"				 if (o == null) {\n" + 
				"					 o = new Object();\n" + 
				"				 }\n" + 
				"			 } finally { /* */ }\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}",
			},
		"");
	}

	// null analysis -- try/finally
	// TODO (maxime) fix
	public void _test0150_try_finally() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 Object m;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 try { /* */ }\n" + 
				"		 finally {\n" + 
				"		   o = m;\n" + 
				"		 }\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"" // because finally assigns to unknown value
		);
	}

	// null analysis -- try/finally
	public void test0151_try_finally() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try { /* */ }\n" + 
				"		 finally {\n" + 
				"			 o = null;\n" + 
				"		 }\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n" // because finally assigns to null 
		);
	}

	// null analysis -- try/finally
	public void test0152_try_finally() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 try {\n" + 
				"		   System.out.println();\n" + // might throw a runtime exception 
				"			 o = new Object();\n" + 
				"		 }\n" + 
				"		 finally { /* */ }\n" + 
				"		 o.toString();\n" + 
						// still OK because in case of exception this code is 
						// not reached
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- try/finally
	public void test0153_try_finally() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(X x) {\n" + 
				"		 x = null;\n" + 
				"		 try {\n" + 
				"			 x = null;\n" +                // complain, already null
				"		 } finally { /* */ }\n" + 
				"	 }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	x = null;\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
	}
	
	// null analysis -- try/finally
	public void test0154_try_finally() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(X x) {\n" + 
				"		 x = null;\n" + 
				"		 try {\n" + 
				"			 x = null;\n" +           
				"		 } finally {\n" + 
				"			 if (x != null) { /* */ }\n" + // complain, null in both paths 
				"		 }\n" + 
				"	 }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	x = null;\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 7)\n" + 
		"	if (x != null) { /* */ }\n" + 
		"	    ^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
	}
	
	// null analysis -- try/finally
	// origin: AssignmentTest#test017
	// REVIEW design choice
	// See also test0174. The whole issue here is whether or not to detect
	// premature exits. We follow JLS's conservative approach, which considers
	// that the try block may exit before the assignment is completed.
	// TODO (maxime) fix
	public void _test0155_try_finally() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	void foo(X x) {\n" + 
				"		x = this;\n" + 
				"		try {\n" + 
				"			x = null;\n" + 
				"		} finally {\n" + 
				"			if (x == null) {/* */}\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
		""
		);
	}

	// null analysis -- try/catch
	public void test0170_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 try {\n" + 
				"		   System.out.println();\n" +  // might throw a runtime exception 
				"			 o = new Object();\n" + 
				"		 }\n" + 
				"		 catch (Throwable t) {\n" + // catches everything 
				"		   return;\n" +             // gets out					
				"	   }\n" + 
				"		 o.toString();\n" +         // can't tell if o is null or not  
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis - try/catch
	public void test0171_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"			 System.out.println();\n" + 
				"			 if (dummy) {\n" + 
				"			   o = null;\n" + 
				"			   throw new Exception();\n" + 
				"			 }\n" + 
				"		 }\n" + 
				"		 catch (Exception e) {\n" + 
				"			 o.toString();\n" +
				 	// quiet: println may throw a RuntimeException
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis - try/catch
	public void test0172_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() throws Exception {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"			 if (dummy) {\n" + 
				"			   o = null;\n" + 
				"			   throw new Exception();\n" + 
				"			 }\n" + 
				"		 }\n" + 
				"		 catch (Exception e) {\n" + 
				"		 }\n" + 
				"		 if (o != null) {\n" +
				  // quiet: get out of try either through normal flow, leaves a new
				  // object, or through Exception, leaves a null 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis - try/catch
	public void test0173_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy, other;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"			 if (dummy) {\n" + 
				"			   if (other) {\n" + 
				"			     throw new LocalException();\n" + // may launch new exception
				"			   }\n" + 
				"			   o = null;\n" + 
				"			   throw new LocalException();\n" + // must launch new exception
				"			 }\n" + 
				"		 }\n" + 
				"		 catch (LocalException e) {\n" + 
				"			 o.toString();\n" +
				 	// quiet: don't know the exact state when exception is launched
				"		 }\n" + 
				"	 }\n" + 
				"	 class LocalException extends Exception {\n" + 
				"		 private static final long serialVersionUID = 1L;\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis - try/catch
	// REVIEW the following series of try catch tests all relate to the finer
	//        analysis of possible exception paths; such analysis
	//        calls for a supplementary context for each condition
	//        (so as to sort out certain paths from hypothetical
	//        ones), which is due to be expensive.
	// TODO (maxime) fix
	public void _test0174_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) throws Exception {\n" + 
				"		 try {\n" + 
				"		   o = null;\n" + 
				"		   throwLocalException();\n" + 
				"		   throw new Exception();\n" + 
				"		 }\n" + 
				"		 catch (LocalException e) {\n" + 
				"		 }\n" + 
				"		 if (o != null) {\n" +
				 	// complain: only way to get out of try and get there is to go
					// through throwLocalException, after the assignment 
				"		 }\n" + 
				"	 }\n" + 
				"	 class LocalException extends Exception {\n" + 
				"		 private static final long serialVersionUID = 1L;\n" + 
				"	 }\n" + 
				"	 void throwLocalException() throws LocalException {\n" + 
				"		 throw new LocalException();\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"
		);
	}

	// null analysis - try/catch
	// TODO (maxime) fix
	public void _test0175_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"	   Object o = new Object();\n" + 
				"		 try {\n" + 
				"		   o = null;\n" + 
				"		   throwException();\n" + 
				"		 }\n" + 
				"		 catch (Exception e) {\n" + 
				"			 o.toString();\n" +
				  // complain: know o is null despite the lack of a definite assignment
				"		 }\n" + 
				"	 }\n" + 
				"	 void throwException() throws Exception {\n" + 
				"		 throw new Exception();\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"
		);
	}

	// null analysis - try/catch
	// TODO (maxime) fix
	public void _test0176_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"		   o = null;\n" + 
				"		   throwException();\n" + 
				"		 }\n" + 
				"		 catch (Throwable t) {\n" + 
				"			 o.toString();\n" +
				  // complain: know o is null despite the lack of a definite assignment
				"		 }\n" + 
				"	 }\n" + 
				"  void throwException() throws Exception {\n" + 
				"		 throw new Exception();\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"
		);
	}

	// null analysis - try/catch
	// TODO (maxime) fix
	public void _test0177_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"			 if (dummy) {\n" + 
				"			   o = null;\n" + 
				"			   throw new Exception();\n" + 
				"			 }\n" + 
				"		 }\n" + 
				"		 catch (Exception e) {\n" + 
				"			 o.toString();\n" +
				 	// complain: know o is null despite the lack of definite assignment
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"
		);
	}

	// null analysis - try/catch
	// TODO (maxime) fix
	public void _test0178_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"			 if (dummy) {\n" + 
				"			   System.out.print(0);\n" + // may thow RuntimeException 
				"			   o = null;\n" + 
				"			   throw new LocalException();\n" + 
				"			 }\n" + 
				"		 }\n" + 
				"		 catch (LocalException e) {\n" + // doesn't catch RuntimeException
				"			 o.toString();\n" +
				 	// complain: know o is null despite the lack of definite assignment
				"		 }\n" + 
				"	 }\n" + 
				"	 class LocalException extends Exception {\n" + 
				"		 private static final long serialVersionUID = 1L;\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"
		);
	}

	// null analysis - try/catch
	// TODO (maxime) fix
	public void _test0179_try_catch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = new Object();\n" + 
				"		 try {\n" + 
				"			 if (dummy) {\n" + 
				"			   o = null;\n" + 
				"			   throw new SubException();\n" + 
				"			 }\n" + 
				"		 }\n" + 
				"		 catch (LocalException e) {\n" + // must catch SubException
				"			 o.toString();\n" +
				 	// complain: know o is null despite the lack of definite assignment
				"		 }\n" + 
				"	 }\n" + 
				"	 class LocalException extends Exception {\n" + 
				"		 private static final long serialVersionUID = 1L;\n" + 
				"	 }\n" + 
				"	 class SubException extends LocalException {\n" + 
				"		 private static final long serialVersionUID = 1L;\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"
		);
	}
	
	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0201_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do {/* */}\n" +
				"		 while (o.toString() != null);\n" +
				      // complain: NPE
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	while (o.toString() != null);\n" + 
			"	       ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0202_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do {/* */}\n" + 
				"		 while (o != null);\n" + 
				  // complain: get o null first time and forever
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	while (o != null);\n" + 
			"	       ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0203_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do {\n" + 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"		 while (o == null);\n" + 
				      // complain: set it to non null before test, for each iteration
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	while (o == null);\n" + 
			"	       ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- do while
	public void test0204_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do {\n" + 
				"		   if (System.currentTimeMillis() > 10L) {\n" + 
				"		     o = new Object();\n" + 
				"		   }\n" + 
				"		 }\n" + 
				"		 while (o == null);\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0205_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo(Object o) {\n" + 
				"	   o = null;\n" +
				"		 do {\n" +
				"		   // do nothing\n" +
				"		 }\n" + 
				"		 while (dummy || o != null);\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	while (dummy || o != null);\n" + 
			"	                    ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0206_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null,\n" + 
				"		        u = new Object(),\n" + 
				"		        v = new Object();\n" + 
				"		 do {\n" +
				"		   if (v == null) {\n" +
				"		     o = new Object();\n" +
				"		   };\n" +
				"		   if (u == null) {\n" +
				"		     v = null;\n" +
				"		   };\n" +
				"		   u = null;\n" +
				"		 }\n" +
				"		 while (o == null);\n" +
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0207_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do {\n" + 
				"		   o.toString();\n" + 
				      	 // complain: NPE on first iteration 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"		 while (dummy);\n" + 
				"	 }\n" + 
				"}\n"},
			"WARN"  
		);
	}

	// null analysis -- do while
	// TODO (maxime) fix
	public void _test0208_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do {\n" + 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"		 while (dummy);\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}
	
	// null analysis -- do while
	public void test0209_do_while() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean dummy;\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 do { /* */ }\n" + 
				"		 while (dummy);\n" + 
				"		 o.toString();\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- for
	// TODO (maxime) fix
	public void _test0221_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 for (;o.toString() != null;) {/* */}\n" +
				      // complain: NPE
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	for (;o.toString() != null;) {/* */}\n" + 
			"	      ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- for
	// TODO (maxime) fix
	public void _test0222_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 for (;o != null;) {/* */}\n" + 
				  // complain: get o null first time and forever
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 4)\n" + 
			"	for (;o != null;) {/* */}\n" + 
			"	      ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- for
	public void test0223_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 for (;o == null;) {\n" + 
				      // quiet: first iteration is sure to find it null, 
				      // but other iterations may change it 
				"		   o = new Object();\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- for
	public void test0224_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"		 Object o = null;\n" + 
				"		 for (;o == null;) {\n" + 
				     // quiet: first iteration is sure to find it null, 
				     // but other iterations may change it 
				"		   if (System.currentTimeMillis() > 10L) {\n" + 
				"		     o = new Object();\n" + 
				"		   }\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""  
		);
	}

	// null analysis -- for
	// TODO (maxime) fix
	public void _test0225_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean bar() {\n" + 
				"		 return true;\n" + 
				"	 }\n" + 
				"	 void foo(Object o) {\n" + 
				"		 for (;bar() && o == null;) {\n" + 
				"		   o.toString();\n" + // complain: NPE because of condition
				"		   o = new Object();\n" +
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}

	// null analysis -- for
	public void test0227_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 for (;o == null; o.toString()) {\n" + 
				"		   o = new Object();\n" +
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- for
	// TODO (maxime) fix
	public void _test0228_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 for (;o == null; o.toString()) {\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	for (;o == null; o.toString()) {\n" + 
			"	                 ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}
	
	// null analysis -- for
	// TODO (maxime) fix
	public void _test0229_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo(Object o) {\n" + 
				"		 for (o.toString(); o == null;) { /* */ }\n" + // complain: protected then unchanged
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	for (o.toString(); o == null;) { /* */ }\n" + 
			"	                   ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}
	
	// null analysis -- for
	public void test0230_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 boolean bar() {\n" + 
				"		 return true;\n" + 
				"	 }\n" + 
				"	 void foo(Object o) {\n" + 
				"	   o = null;\n" + 
				"		 for (o.toString(); bar();) {\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	for (o.toString(); bar();) {\n" + 
			"	     ^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}
	
	// null analysis -- for
	// TODO (maxime) fix
	public void _test0231_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Object t[] = null;\n" + 
					"		 for (Object o : t) {/* */}\n" +
					      // complain: NPE
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	for (Object o : t) {/* */}\n" + 
				"	                ^\n" + 
				"The variable t can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- for
	// TODO (maxime) fix
	public void _test0232_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Iterable i = null;\n" + 
					"		 for (Object o : i) {/* */}\n" +
					      // complain: NPE
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 4)\n" + 
				"	for (Object o : i) {/* */}\n" + 
				"	                ^\n" + 
				"The variable i can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- for
	public void test0233_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Object t[] = new Object[1];\n" + 
					"		 for (Object o : t) {/* */}\n" +
					"	 }\n" + 
					"}\n"},
				""
			);
		}
	}

	// null analysis -- for
	public void test0234_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Iterable i = new java.util.Vector<Object>();\n" + 
					"		 for (Object o : i) {/* */}\n" +
					"	 }\n" + 
					"}\n"},
				""
			);
		}
	}

	// null analysis -- for
	public void test0235_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Iterable i = new java.util.Vector<Object>();\n" + 
					"		 Object flag = null;\n" + 
					"		 for (Object o : i) {\n" +
					"		   flag = new Object();\n" +
					"		 }\n" +
					"		 flag.toString();\n" + 
					"	 }\n" + 
					"}\n"},
				""
			);
		}
	}
	
	// null analysis -- for
	// TODO (maxime) fix
	public void _test0236_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo() {\n" + 
					"		 Iterable i = new java.util.Vector<Object>();\n" + 
					"		 Object flag = null;\n" + 
					"		 for (Object o : i) { /* */ }\n" +
					"		 flag.toString();\n" + 
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 6)\n" + 
				"	flag.toString();\n" + 
				"	^^^^\n" + 
				"The variable flag can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}

	// null analysis -- for
	public void test0237_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo(boolean dummy) {\n" + 
					"		 Object flag = null;\n" + 
					"		 for (;dummy;) {\n" +
					"		   flag = new Object();\n" +
					"		 }\n" +
					"		 flag.toString();\n" + 
					"	 }\n" + 
					"}\n"},
				""
			);
		}
	}
	
	// null analysis -- for
	// TODO (maxime) fix
	public void _test0238_for() {
		if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
			this.runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" + 
					"	 void foo(boolean dummy) {\n" + 
					"		 Object flag = null;\n" + 
					"		 for (;dummy;) { /* */ }\n" +
					"		 flag.toString();\n" + 
					"	 }\n" + 
					"}\n"},
				"----------\n" + 
				"1. WARNING in X.java (at line 5)\n" + 
				"	flag.toString();\n" + 
				"	^^^^\n" + 
				"The variable flag can only be null; it was either set to null or checked for null when last used\n" + 
				"----------\n"
			);
		}
	}
	
	// null analysis -- for
	// origin: AssignmentTest#test019
	public void test0239_for() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 public static final char[] foo(char[] a, char c1, char c2) {\n" + 
				"		char[] r = null;\n" + 
				"		for (int i = 0, length = a.length; i < length; i++) {\n" + 
				"			char c = a[i];\n" + 
				"			if (c == c1) {\n" + 
				"				if (r == null) {\n" + 
				"					r = new char[length];\n" + 
				"				}\n" + 
				"				r[i] = c2;\n" + 
				"			} else if (r != null) {\n" + 
				"				r[i] = c;\n" + 
				"			}\n" + 
				"		}\n" + 
				"		if (r == null) return a;\n" + 
				"		return r;\n" + 
				"	}\n" + 
				"}\n",
			},
		"");
	}

	// null analysis -- for
	public void test0240_for_continue_break() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	 void foo() {\n" + 
				"	   Object o = new Object();\n" + 
				"		 for (int i = 0; i < 10; i++) {\n" + 
				"		   if (o == null) {\n" + // complain: o cannot be null
				"		     continue;\n" + 
				"		   }\n" + 
				"		   o = null;\n" + 
				"		   break;\n" + 
				"		 }\n" + 
				"	 }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	if (o == null) {\n" + 
		"	    ^\n" + 
		"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
	}
		
	// null analysis -- switch
	public void test0300_switch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int k;\n" + 
				"	void foo() {\n" + 
				"		Object o = null;\n" + 
				"		switch (k) {\n" + 
				"			case 0 :\n" + 
				"				o = new Object();\n" + 
				"				break;\n" + 
				"			case 2 :\n" + 
				"				return;\n" + 
				"		}\n" + 
				"		if(o == null) { /* */	}\n" + // quiet: don't know whether came from 0 or default
				"	}\n" + 
				"}\n"},
			""
		);
	}

	// null analysis -- switch
	public void test0301_switch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int k;\n" + 
				"	void foo() {\n" + 
				"		Object o = null;\n" + 
				"		switch (k) {\n" + 
				"			case 0 :\n" + 
				"				o = new Object();\n" + 
				"				break;\n" + 
				"			default :\n" + 
				"				return;\n" + 
				"		}\n" + 
				"		if(o == null) { /* */	}\n" + // complain: only get there through 0, o non null
				"	}\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 12)\n" + 
			"	if(o == null) { /* */	}\n" + 
			"	   ^\n" + 
			"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
			"----------\n"
		);
	}
	
	// null analysis -- switch
	public void test0302_switch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int k;\n" + 
				"	void foo() {\n" + 
				"		Object o = null;\n" + 
				"		switch (k) {\n" + 
				"			case 0 :\n" + 
				"				o.toString();\n" + // complain: o can only be null
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"},
			"----------\n" + 
			"1. WARNING in X.java (at line 7)\n" + 
			"	o.toString();\n" + 
			"	^\n" + 
			"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
			"----------\n"
		);
	}
	
	// null analysis -- switch
	public void test0303_switch() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	int k;\n" + 
				"	void foo() {\n" + 
				"		Object o = null;\n" + 
				"		switch (k) {\n" + 
				"			case 0 :\n" + 
				"			  o = new Object();\n" + 
				"			case 1 :\n" + 
				"				o.toString();\n" + // quiet: may come through 0 or 1
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
		"");
	}
	
	// flow info low-level validation
	// TODO (maxime) try to cover with source level tests instead of intrusive code
}
