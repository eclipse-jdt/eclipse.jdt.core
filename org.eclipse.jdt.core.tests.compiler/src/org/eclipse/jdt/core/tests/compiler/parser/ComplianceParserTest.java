/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;

public class ComplianceParserTest extends AbstractRegressionTest {
public ComplianceParserTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public static Class testClass() {
	return ComplianceParserTest.class;
}
public void runComplianceParserTest(
	String[] testFiles,
	String expected13ProblemeLog,
	String expected14ProblemeLog,
	String expected15ProblemeLog){
	if(COMPLIANCE_1_3.equals(this.complianceLevel)) {
		this.runNegativeTest(testFiles, expected13ProblemeLog);
	} else if(COMPLIANCE_1_4.equals(this.complianceLevel)) {
		this.runNegativeTest(testFiles, expected14ProblemeLog);
	} else if(COMPLIANCE_1_5.equals(this.complianceLevel)) {
		this.runNegativeTest(testFiles, expected15ProblemeLog);
	}
}
public void test0001() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.BBB.*;\n" +
		"public class X {\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.*;\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, static imports are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.*;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.*;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0002() {
	String[] testFiles = new String[] {
		"X.java",
		"import static aaa.BBB.CCC;\n" +
		"public class X {\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.CCC;\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, static imports are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.CCC;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	import static aaa.BBB.CCC;\n" + 
		"	              ^^^\n" + 
		"The import aaa cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0003() {
	String[] testFiles = new String[] {
		"X.java",
		"public enum X {\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public enum X {\n" + 
		"	       ^^^^\n" + 
		"Syntax error on token \"enum\", enum expected\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0004() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(){\n" +
		"		for(String o: c) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	    ^^^^^^^^^^^\n" + 
		"Syntax error, \'for each\' statements are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	              ^\n" + 
		"c cannot be resolved\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	for(String o: c) {\n" + 
		"	              ^\n" + 
		"c cannot be resolved\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0005() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	void foo(Z ... arg){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	               ^^^\n" + 
		"Syntax error, varargs are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(Z ... arg){\n" + 
		"	         ^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0006() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                                               ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T1 extends String, T2> extends Y {\n" + 
		"	                                               ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0007() {
	String[] testFiles = new String[] {
		"X.java",
		"public interface X <T1 extends String, T2> extends Y {\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public interface X <T1 extends String, T2> extends Y {\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 1)\n" + 
		"	public interface X <T1 extends String, T2> extends Y {\n" + 
		"	                                                   ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public interface X <T1 extends String, T2> extends Y {\n" + 
		"	                                                   ^\n" + 
		"Y cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0008() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> int foo(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> int foo(){\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> int foo(){\n" + 
		"	                                   ^^^^^\n" + 
		"This method must return a result of type int\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0009() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public <T1 extends String, T2> X(){\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	public <T1 extends String, T2> X(){\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Syntax error, type parameters are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0010() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	Z<Y1, Y2> var;\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	  ^^^^^^\n" + 
		"Syntax error, parameterized types are only available if source level is 1.5\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Z<Y1, Y2> var;\n" + 
		"	^\n" + 
		"Z cannot be resolved to a type\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
public void test0011() {
	String[] testFiles = new String[] {
		"X.java",
		"public class X {\n" +
		"	public X(){\n" +
		"		<Y1, Y2>this(null);\n" +
		"	}\n" +
		"}\n"
	};
	
	String expected13ProblemeLog =
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	 ^^^^^^\n" + 
		"Syntax error, parameterized types are only available if source level is 1.5\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	        ^^^^^^^^^^\n" + 
		"The constructor X(null) is undefined\n" + 
		"----------\n";
	String expected14ProblemeLog =
		expected13ProblemeLog;
	
	String expected15ProblemeLog = 
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	<Y1, Y2>this(null);\n" + 
		"	        ^^^^^^^^^^\n" + 
		"The constructor X(null) is undefined\n" + 
		"----------\n";
	
	runComplianceParserTest(
		testFiles,
		expected13ProblemeLog,
		expected14ProblemeLog,
		expected15ProblemeLog
	);
}
}
