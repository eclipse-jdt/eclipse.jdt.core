/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
public class TryWithResourcesStatementTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryWithResourcesStatementTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
// Test resource type related errors 
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (int i = 0) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (int i = 0) {\n" + 
		"	     ^^^\n" + 
		"The resource type int has to be a subclass of java.lang.AutoCloseable \n" + 
		"----------\n");
}
// Test resource type related errors 
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (int[] tab = {}) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (int[] tab = {}) {\n" + 
		"	     ^^^^^\n" + 
		"The resource type int[] has to be a subclass of java.lang.AutoCloseable \n" + 
		"----------\n");
}
// Test that resource type could be interface type. 
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable{\n" +
			"	public void method1(){\n" +
			"		try (AutoCloseable a = new X()) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X implements AutoCloseable{\n" + 
		"	             ^\n" + 
		"The type X must implement the inherited abstract method AutoCloseable.close()\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (AutoCloseable a = new X()) {\n" + 
		"	                   ^^^^^^^^^^^\n" + 
		"Unhandled exception type Exception\n" + 
		"----------\n");
}
// Type resource type related errors 
public void test003a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y y = new Y()) { \n" +
			"			System.out.println();\n" +
			"		} catch (Exception e) {\n" +
			"		} finally {\n" +
			"           Zork z;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements Managed {\n" +
			"    public void close () throws Exception {\n" +
			"    }\n" +
			"}\n" +
			"interface Managed extends AutoCloseable {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
// Scope, visibility related tests.
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) throws IOException {\n" + 
			"		int i = 0;\n" + 
			"		try (LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" + 
			"			String s;\n" + 
			"			int i = 0;\n" + 
			"			while ((s = reader.readLine()) != null) {\n" + 
			"				System.out.println(s);\n" + 
			"				i++;\n" + 
			"			}\n" + 
			"			System.out.println(\"\" + i + \" lines\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	int i = 0;\n" + 
		"	    ^\n" + 
		"Duplicate local variable i\n" + 
		"----------\n");
}
//Scope, visibility related tests.
public void test004a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) throws IOException {\n" + 
			"		try (LineNumberReader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" + 
			"			String s;\n" + 
			"			int r = 0;\n" + 
			"			while ((s = r.readLine()) != null) {\n" + 
			"				System.out.println(s);\n" + 
			"				r++;\n" + 
			"			}\n" + 
			"			System.out.println(\"\" + r + \" lines\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	int r = 0;\n" + 
		"	    ^\n" + 
		"Duplicate local variable r\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	while ((s = r.readLine()) != null) {\n" + 
		"	            ^^^^^^^^^^^^\n" + 
		"Cannot invoke readLine() on the primitive type int\n" + 
		"----------\n");
}
// check that resources are implicitly final
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) throws IOException {\n" + 
			"		try (Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" + 
			"			r = new FileReader(args[0]);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	r = new FileReader(args[0]);\n" + 
		"	^\n" + 
		"The resource r of a try-with-resources statement cannot be assigned\n" + 
		"----------\n");
}
//check that try statement can be empty
public void test006() {
	this.runNegativeTest( // cannot be a conform test as this triggers an AIOOB.
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) throws IOException {\n" + 
			"		try (Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" + 
			"		} catch(Zork z) {" +
			"       }\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	} catch(Zork z) {       }\n" + 
		"	        ^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}
//check that resources are implicitly final but they can be explicitly final 
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) throws IOException {\n" + 
			"		try (final Reader r = new LineNumberReader(new BufferedReader(new FileReader(args[0])))) {\n" + 
			"			r = new FileReader(args[0]);\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	r = new FileReader(args[0]);\n" + 
		"	^\n" + 
		"The resource r of a try-with-resources statement cannot be assigned\n" + 
		"----------\n");
}
// resource type tests
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y [] i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y [] i = null) {\n" + 
		"	     ^^^^\n" + 
		"The resource type Y[] has to be a subclass of java.lang.AutoCloseable \n" + 
		"----------\n");
}
// Resource Type tests
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i [] = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y i [] = null) {\n" + 
		"	     ^\n" + 
		"The resource type Y[] has to be a subclass of java.lang.AutoCloseable \n" + 
		"----------\n");
}
// Scope, visibility tests
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(int p){\n" +
			"       int k;\n" +
			"		try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" + 
		"	                      ^^^^^^^^^^^^\n" + 
		"Duplicate local variable i\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" + 
		"	                                     ^^^^^^^^^^^^\n" + 
		"Duplicate local variable p\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" + 
		"	                                                    ^^^^^^^^^^^^\n" + 
		"Duplicate local variable k\n" + 
		"----------\n");
}
// Scope, visibility tests
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"       catch (Exception e) {\n" +
			"           System.out.println(i);\n" +
			"       }\n" +
			"       finally {\n" +
			"           System.out.println(p);\n" +
			"       }\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	System.out.println(i);\n" + 
		"	                   ^\n" + 
		"i cannot be resolved to a variable\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	System.out.println(p);\n" + 
		"	                   ^\n" + 
		"p cannot be resolved to a variable\n" + 
		"---" +
		"-------\n");
}
// Scope, visibility related tests.
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" +
			"           try {\n" +
			"			    System.out.println();\n" +
			"           } catch (Exception i) {\n" +
			"           }\n" +
			"		}\n" +
			"       catch (Exception e) {\n" +
			"           System.out.println(i);\n" +
			"       }\n" +
			"       finally {\n" +
			"           System.out.println(p);\n" +
			"       }\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	} catch (Exception i) {\n" + 
		"	                   ^\n" + 
		"Duplicate parameter i\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	System.out.println(i);\n" + 
		"	                   ^\n" + 
		"i cannot be resolved to a variable\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(p);\n" + 
		"	                   ^\n" + 
		"p cannot be resolved to a variable\n" + 
		"----------\n");
}
// Shadowing behavior tests
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"	try (Y y = new Y(); Y p = new Y()) {\n" +
			"	    X x = new X() {\n" +
			"		      public void foo(int p) {\n" +
			"                         try {\n" +
			"		             System.out.println();\n" +
			"		          } catch (Exception y) {\n" +
			"		          }\n" +
			"		       }\n" +
			"	           };\n" +
			"	} finally {\n" +
			"            System.out.println(y);\n" +
			"	}\n" +
			"   }\n" +
			"}\n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() {\n" +
			"		    System.out.println();\n" +
			"	}\n" +
			"}\n",
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	public void foo(int p) {\n" + 
		"	                    ^\n" + 
		"The parameter p is hiding another local variable defined in an enclosing type scope\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 8)\n" + 
		"	} catch (Exception y) {\n" + 
		"	                   ^\n" + 
		"The parameter y is hiding another local variable defined in an enclosing type scope\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(y);\n" + 
		"	                   ^\n" + 
		"y cannot be resolved to a variable\n" + 
		"----------\n");
}
// Test for unhandled exceptions
public void test014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null) {}\n" +
			"           Y why = new Y();\n" +
			"		    System.out.println(\"Try block\");\n" +
			"		} finally {\n" +
			"		    System.out.println(\"Finally block\");\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws WeirdException {\n" +
			"		throw new WeirdException();\n" +
			"	}\n" +
			"	public void close() {\n" +
			"		    System.out.println(\"Closing resource\");\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class WeirdException extends Throwable {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y();) {\n" + 
		"	           ^^^^^^^\n" + 
		"Unhandled exception type WeirdException\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 4)\n" + 
		"	if (y == null) {}\n" + 
		"	               ^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 5)\n" + 
		"	Y why = new Y();\n" + 
		"	        ^^^^^^^\n" + 
		"Unhandled exception type WeirdException\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 22)\n" + 
		"	class WeirdException extends Throwable {}\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"The serializable class WeirdException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
// Resource nullness tests
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null)\n {}\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	{}\n" + 
		"	^^\n" + 
		"Dead code\n" + 
		"----------\n");
}
// Dead code tests, resource nullness, unhandled exception tests
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null) {}\n" +
			"           Y why = new Y();\n" +
			"		    System.out.println(\"Try block\");\n" +
			"		}\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws WeirdException {\n" +
			"		throw new WeirdException();\n" +
			"	}\n" +
			"	public void close() {\n" +
			"		    System.out.println(\"Closing resource\");\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class WeirdException extends Throwable {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y();) {\n" + 
		"	           ^^^^^^^\n" + 
		"Unhandled exception type WeirdException\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 4)\n" + 
		"	if (y == null) {}\n" + 
		"	               ^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 5)\n" + 
		"	Y why = new Y();\n" + 
		"	        ^^^^^^^\n" + 
		"Unhandled exception type WeirdException\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 20)\n" + 
		"	class WeirdException extends Throwable {}\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"The serializable class WeirdException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
// Dead code tests
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try (Y y = new Y();) {\n" +
			"           if (y == null)\n {}\n" +
			"		} finally {\n" +
			"       }\n" +
			"	}\n" +
			"} \n" +
			"\n" +
			"class Y implements AutoCloseable {\n" +
			"	public void close() {\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	{}\n" + 
		"	^^\n" + 
		"Dead code\n" + 
		"----------\n");
}
// Syntax error tests
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {    \n" +
			"		try () {\n" +
			"		} finally {\n" +
			"       }\n" +
			"	}\n" +
			"} \n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try () {\n" + 
		"	    ^\n" + 
		"Syntax error on token \"(\", Resources expected after this token\n" + 
		"----------\n");
}
// Unhandled exception tests
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"	    	 } finally {\n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	       ^^^^^^^^^^^^\n" + 
		"Unhandled exception type XXException\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                      ^^^^^^^^^^^^\n" + 
		"Unhandled exception type YYException\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                                     ^^^^^^^^^^^\n" + 
		"Unhandled exception type ZZException\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	throw new XXException();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type XXException\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 34)\n" + 
		"	class XException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 35)\n" + 
		"	class XXException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 36)\n" + 
		"	class YException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 37)\n" + 
		"	class YYException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"9. WARNING in X.java (at line 38)\n" + 
		"	class ZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"10. WARNING in X.java (at line 39)\n" + 
		"	class ZZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
// Resource type test
public void test021() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y {\n" +
			"    public void close () {}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y i = null) {\n" + 
		"	     ^\n" + 
		"The resource type Y has to be a subclass of java.lang.AutoCloseable \n" + 
		"----------\n");
}
// Interface method return type compatibility test
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public int close () { return 0; }\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	public int close () { return 0; }\n" + 
		"	       ^^^\n" + 
		"The return type is incompatible with AutoCloseable.close()\n" + 
		"----------\n");
}
// Exception handling, compatibility tests
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y i = null) {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public void close () throws Blah {}\n" +
			"}\n" +
			"class Blah extends Throwable {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y i = null) {\n" + 
		"	       ^^^^^^^^\n" + 
		"Unhandled exception type Blah\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	public void close () throws Blah {}\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^\n" + 
		"Exception Blah is not compatible with throws clause in AutoCloseable.close()\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 11)\n" + 
		"	class Blah extends Throwable {}\n" + 
		"	      ^^^^\n" + 
		"The serializable class Blah does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
// Exception handling tests
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"            } catch (XXException x) {\n" +
			"	 		 } catch (YYException y) {\n" +
			"            } catch (ZZException z) {\n" +
			"	    	 } finally {\n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 37)\n" + 
		"	class XException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 38)\n" + 
		"	class XXException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 39)\n" + 
		"	class YException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 40)\n" + 
		"	class YYException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 41)\n" + 
		"	class ZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 42)\n" + 
		"	class ZZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
// Unhandled exception tests
public void test025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"            \n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	       ^^^^^^^^^^^^\n" + 
		"Unhandled exception type XXException\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                      ^^^^^^^^^^^^\n" + 
		"Unhandled exception type YYException\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                                     ^^^^^^^^^^^\n" + 
		"Unhandled exception type ZZException\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	throw new XXException();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type XXException\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 34)\n" + 
		"	class XException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 35)\n" + 
		"	class XXException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"7. WARNING in X.java (at line 36)\n" + 
		"	class YException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 37)\n" + 
		"	class YYException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"9. WARNING in X.java (at line 38)\n" + 
		"	class ZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"10. WARNING in X.java (at line 39)\n" + 
		"	class ZZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"	public static void main(String [] args) {\n" +
			"            try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" +
			"            throw new XXException();\n" +
			"            } catch (XException x) {\n" +
			"	 		 } catch (YException y) {\n" +
			"            } catch (ZException z) {\n" +
			"            } catch (XXException x) {\n" +
			"	 		 } catch (YYException y) {\n" +
			"            } catch (ZZException z) {\n\n" +
			"            }\n" +
			"	}\n" +
			"	public X() throws XException {\n" +
			"		throw new XException();\n" +
			"	}\n" +
			"	public void close() throws XXException {\n" +
			"		throw new XXException();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	public Y() throws YException {\n" +
			"		throw new YException();\n" +
			"	}\n" +
			"	public void close() throws YYException {\n" +
			"		throw new YYException();\n" +
			"	}\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"	public Z() throws ZException {\n" +
			"		throw new ZException();\n" +
			"	}\n" +
			"	public void close() throws ZZException {\n" +
			"		throw new ZZException();\n" +
			"	}\n" +
			"}\n" +
			"class XException extends Exception {}\n" +
			"class XXException extends Exception {}\n" +
			"class YException extends Exception {}\n" +
			"class YYException extends Exception {}\n" +
			"class ZException extends Exception {}\n" +
			"class ZZException extends Exception {}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 37)\n" + 
		"	class XException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class XException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 38)\n" + 
		"	class XXException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class XXException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 39)\n" + 
		"	class YException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class YException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 40)\n" + 
		"	class YYException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class YYException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 41)\n" + 
		"	class ZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^\n" + 
		"The serializable class ZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n" + 
		"6. WARNING in X.java (at line 42)\n" + 
		"	class ZZException extends Exception {}\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"The serializable class ZZException does not declare a static final serialVersionUID field of type long\n" + 
		"----------\n");
}
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        try (X x = new X(); Y y = new Y()) {\n" +
			"            System.out.println(\"Body\");\n" +
			"            throw new Exception(\"Body\");\n" +
			"        } catch (Exception e) {\n" +
			"            System.out.println(e);\n" +
			"            Throwable [] suppressed = e.getSuppressed();\n" +
			"            for (int i = 0; i < suppressed.length; i++) {\n" +
			"                System.out.println(\"Suppressed:\" + suppressed[i]);\n" +
			"            }\n" +
			"        } finally {\n" +
			"            int finallyVar = 10;\n" +
			"            System.out.println(finallyVar);\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X CTOR\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X Close\");\n" +
			"        throw new Exception(\"X Close\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y CTOR\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y Close\");\n" +
			"        throw new Exception(\"Y Close\");\n" +
			"    }\n" +
			"}\n"
		},
		"X CTOR\n" + 
		"Y CTOR\n" + 
		"Body\n" + 
		"Y Close\n" + 
		"X Close\n" + 
		"java.lang.Exception: Body\n" + 
		"Suppressed:java.lang.Exception: Y Close\n" + 
		"Suppressed:java.lang.Exception: X Close\n" + 
		"10");
}
public void test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception {\n" +
			"        try (X x = new X(); Y y = new Y()) {\n" +
			"            System.out.println(\"Body\");\n" +
			"        } catch (Exception e) {\n" +
			"            e.printStackTrace();\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X CTOR\");\n" +
			"    }\n" +
			"    public void close() {\n" +
			"        System.out.println(\"X DTOR\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y CTOR\");\n" +
			"    }\n" +
			"    public void close() {\n" +
			"        System.out.println(\"Y DTOR\");\n" +
			"    }\n" +
			"}\n"
		},
		"X CTOR\n" + 
		"Y CTOR\n" + 
		"Body\n" + 
		"Y DTOR\n" + 
		"X DTOR");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338881
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        try(FileReader fileReader = new FileReader(file);) {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } catch (IOException e) {\n" +
			"            System.out.println(\"Got IO exception\");\n" +
			"        } finally{\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"Got IO exception");
}
public void test030() {  // test return + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = true;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            while (true) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"            			if (getOut) \n" +
			"            				return;\n" +
			"            			else\n" +
			"            				break;\n" +
			"            		}\n" +
			"            	}\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" + 
		"X::X\n" + 
		"X::X\n" + 
		"Outer Try\n" + 
		"Y::Y\n" + 
		"Y::Y\n" + 
		"Middle Try\n" + 
		"Z::Z\n" + 
		"Z::Z\n" + 
		"Inner Try\n" + 
		"Z::~Z\n" + 
		"Z::~Z\n" + 
		"Y::~Y\n" + 
		"Y::~Y\n" + 
		"X::~X\n" + 
		"X::~X");
}
public void test031() { // test break + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = false;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            while (true) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"            			if (getOut) \n" +
			"            				return;\n" +
			"            			else\n" +
			"            				break;\n" +
			"            		}\n" +
			"            	}\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" + 
		"X::X\n" + 
		"X::X\n" + 
		"Outer Try\n" + 
		"Y::Y\n" + 
		"Y::Y\n" + 
		"Middle Try\n" + 
		"Z::Z\n" + 
		"Z::Z\n" + 
		"Inner Try\n" + 
		"Z::~Z\n" + 
		"Z::~Z\n" + 
		"Y::~Y\n" + 
		"Y::~Y\n" + 
		"Out of while\n" + 
		"X::~X\n" + 
		"X::~X");
}
public void test032() { // test continue + resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = false;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = new X(); X x2 = new X()) {\n" +
			"            System.out.println(\"Outer Try\");\n" +
			"            boolean more = true;\n" +
			"            while (more) {\n" +
			"            	try (Y y1 = new Y(); Y y2 = new Y()) {\n" +
			"            		System.out.println(\"Middle Try\");\n" +
			"            		try (Z z1 = new Z(); Z z2 = new Z()) {\n" +
			"            			System.out.println(\"Inner Try\");\n" +
			"                       more = false;\n" +
			"                       continue;\n" +
			"            		} finally { \n" +
			"                       System.out.println(\"Inner Finally\");\n" +
			"                   }\n" +
			"            	} finally {\n" +
			"                   System.out.println(\"Middle Finally\");\n" +
			"               }\n" +
			"            }\n" +
			"            System.out.println(\"Out of while\");\n" +
			"        } finally {\n" +
			"            System.out.println(\"Outer Finally\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" + 
		"X::X\n" + 
		"X::X\n" + 
		"Outer Try\n" + 
		"Y::Y\n" + 
		"Y::Y\n" + 
		"Middle Try\n" + 
		"Z::Z\n" + 
		"Z::Z\n" + 
		"Inner Try\n" + 
		"Z::~Z\n" + 
		"Z::~Z\n" + 
		"Inner Finally\n" + 
		"Y::~Y\n" + 
		"Y::~Y\n" + 
		"Middle Finally\n" + 
		"Out of while\n" + 
		"X::~X\n" + 
		"X::~X\n" + 
		"Outer Finally");
}
public void test033() { // test null resources
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X implements AutoCloseable {\n" +
			"    public static void main(String [] args) throws Exception { \n" +
			"    	final boolean getOut = false;\n" +
			"    	System.out.println(\"Main\");\n" +
			"    	try (X x1 = null; Y y = new Y(); Z z = null) {\n" +
			"            System.out.println(\"Body\");\n" +
			"        } finally {\n" +
			"            System.out.println(\"Outer Finally\");\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"    }\n" +
			"}\n"
		},
		"Main\n" + 
		"Y::Y\n" + 
		"Body\n" + 
		"Y::~Y\n" + 
		"Outer Finally");
}
public static Class testClass() {
	return TryWithResourcesStatementTest.class;
}
}
