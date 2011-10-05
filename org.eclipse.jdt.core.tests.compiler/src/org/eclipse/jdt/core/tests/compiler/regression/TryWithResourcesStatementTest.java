/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *     							bug 358827 - [1.7] exception analysis for t-w-r spoils null analysis
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *     							bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;
public class TryWithResourcesStatementTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test056throw"};
//	TESTS_NUMBERS = new int[] { 50 };
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
		"The resource type int does not implement java.lang.AutoCloseable\n" + 
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
		"The resource type int[] does not implement java.lang.AutoCloseable\n" + 
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
		"	                   ^\n" + 
		"Unhandled exception type Exception thrown by automatic close() invocation on a\n" + 
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
		"The resource type Y[] does not implement java.lang.AutoCloseable\n" + 
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
		"The resource type Y[] does not implement java.lang.AutoCloseable\n" + 
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
		"	                      ^\n" + 
		"Duplicate local variable i\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" + 
		"	                                     ^\n" + 
		"Duplicate local variable p\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	try (Y i = new Y(); Y i = new Y(); Y p = new Y(); Y k = new Y();) {\n" + 
		"	                                                    ^\n" + 
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
		"3. WARNING in X.java (at line 5)\n" + 
		"	Y why = new Y();\n" + 
		"	  ^^^\n" + 
		"Resource leak: 'why' is never closed\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 5)\n" + 
		"	Y why = new Y();\n" + 
		"	        ^^^^^^^\n" + 
		"Unhandled exception type WeirdException\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 22)\n" + 
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
		"3. WARNING in X.java (at line 5)\n" + 
		"	Y why = new Y();\n" + 
		"	  ^^^\n" + 
		"Resource leak: 'why' is never closed\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 5)\n" + 
		"	Y why = new Y();\n" + 
		"	        ^^^^^^^\n" + 
		"Unhandled exception type WeirdException\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 20)\n" + 
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
		"	       ^\n" + 
		"Unhandled exception type XXException thrown by automatic close() invocation on x\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                      ^\n" + 
		"Unhandled exception type YYException thrown by automatic close() invocation on y\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                                     ^\n" + 
		"Unhandled exception type ZZException thrown by automatic close() invocation on z\n" + 
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
		"The resource type Y does not implement java.lang.AutoCloseable\n" + 
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
		"	       ^\n" + 
		"Unhandled exception type Blah thrown by automatic close() invocation on i\n" + 
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
		"	       ^\n" + 
		"Unhandled exception type XXException thrown by automatic close() invocation on x\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                      ^\n" + 
		"Unhandled exception type YYException thrown by automatic close() invocation on y\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	try (X x = new X(); Y y = new Y(); Z z = new Z()) {\n" + 
		"	                                     ^\n" + 
		"Unhandled exception type ZZException thrown by automatic close() invocation on z\n" + 
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
public void test030a() {  // test return + resources + with exceptions being thrown by close()
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
			"        } catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"        }\n" +
			"    }\n" +
			"    public X() {\n" +
			"        System.out.println(\"X::X\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"X::~X\");\n" +
			"        throw new Exception(\"X::~X\");\n" +
			"    }\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"    public Y() {\n" +
			"        System.out.println(\"Y::Y\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Y::~Y\");\n" +
			"        throw new Exception(\"Y::~Y\");\n" +
			"    }\n" +
			"}\n" +
			"class Z implements AutoCloseable {\n" +
			"    public Z() {\n" +
			"        System.out.println(\"Z::Z\");\n" +
			"    }\n" +
			"    public void close() throws Exception {\n" +
			"        System.out.println(\"Z::~Z\");\n" +
			"        throw new Exception(\"Z::~Z\");\n" +
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
		"X::~X\n" + 
		"java.lang.Exception: Z::~Z\n" + 
		"Suppressed: java.lang.Exception: Z::~Z\n" + 
		"Suppressed: java.lang.Exception: Y::~Y\n" + 
		"Suppressed: java.lang.Exception: Y::~Y\n" + 
		"Suppressed: java.lang.Exception: X::~X\n" + 
		"Suppressed: java.lang.Exception: X::~X");
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
public void test034() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"		throw new Exception (\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"		throw new Exception (\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"		throw new Exception (\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"java.lang.Exception: A::A\n" + 
		"All done");
}
public void test035() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"		throw new Exception (\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"		throw new Exception (\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: B::B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test036() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"		throw new Exception (\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: C::C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test037() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"		throw new Exception (\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: D::D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test038() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"		throw new Exception (\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: E::E\n" + 
		"Suppressed: java.lang.Exception: D::~D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"		throw new Exception (\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: F::F\n" + 
		"Suppressed: java.lang.Exception: E::~E\n" + 
		"Suppressed: java.lang.Exception: D::~D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test040() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"					throw new Exception(\"Body\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: Body\n" + 
		"Suppressed: java.lang.Exception: F::~F\n" + 
		"Suppressed: java.lang.Exception: E::~E\n" + 
		"Suppressed: java.lang.Exception: D::~D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test041() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"		throw new Exception (\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: F::~F\n" + 
		"Suppressed: java.lang.Exception: E::~E\n" + 
		"Suppressed: java.lang.Exception: D::~D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test042() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: E::~E\n" + 
		"Suppressed: java.lang.Exception: D::~D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"		throw new Exception (\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: D::~D\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: C::~C\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		throw new Exception (\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		throw new Exception (\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"java.lang.Exception: A::~A\n" + 
		"All done");
}
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A(); B b = new B()) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"			try (C c = new C(); D d = new D();) {\n" +
			"				System.out.println(\"Middle try\");\n" +
			"				try (E e = new E(); F f = new F()) {\n" +
			"					System.out.println(\"Inner try\");\n" +
			"				} \n" +
			"			}\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"	}\n" +
			"}\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"	}\n" +
			"}\n" +
			"class D implements AutoCloseable {\n" +
			"	public D () throws Exception {\n" +
			"		System.out.println(\"D::D\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"D::~D\");\n" +
			"	}\n" +
			"}\n" +
			"class E implements AutoCloseable {\n" +
			"	public E () throws Exception {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"	}\n" +
			"}\n" +
			"class F implements AutoCloseable {\n" +
			"	public F () throws Exception {\n" +
			"		System.out.println(\"F::F\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"F::~F\");\n" +
			"	}\n" +
			"}\n" +
			"class G implements AutoCloseable {\n" +
			"	public G () throws Exception {\n" +
			"		System.out.println(\"G::G\");\n" +
			"		throw new Exception (\"G::G\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"G::~G\");\n" +
			"		throw new Exception (\"G::~G\");\n" +
			"	}\n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"B::B\n" + 
		"Outer try\n" + 
		"C::C\n" + 
		"D::D\n" + 
		"Middle try\n" + 
		"E::E\n" + 
		"F::F\n" + 
		"Inner try\n" + 
		"F::~F\n" + 
		"E::~E\n" + 
		"D::~D\n" + 
		"C::~C\n" + 
		"B::~B\n" + 
		"A::~A\n" + 
		"All done");
}
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (A a = new A()) {\n" +
			"			System.out.println(\"X::Try\");\n" +
			"			throw new Exception(\"X::Main\");\n" +
			"		} catch (Exception e) {\n" +
			"				System.out.println(e);\n" +
			"				Throwable suppressed [] = e.getSuppressed();\n" +
			"				for (int i = 0; i < suppressed.length; ++i) {\n" +
			"					System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"				}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class A implements AutoCloseable {\n" +
			"	public A () throws Exception {\n" +
			"		System.out.println(\"A::A\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"A::~A\");\n" +
			"		try (B b = new B()) {\n" +
			"			System.out.println(\"A::~A::Try\");\n" +
			"			throw new Exception(\"A::~A\");\n" +
			"		} catch (Exception e) {\n" +
			"				System.out.println(e);\n" +
			"				Throwable suppressed [] = e.getSuppressed();\n" +
			"				for (int i = 0; i < suppressed.length; ++i) {\n" +
			"					System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"				}\n" +
			"				throw e;\n" +
			"		} 	\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"class B implements AutoCloseable {\n" +
			"	public B () throws Exception {\n" +
			"		System.out.println(\"B::B\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"B::~B\");\n" +
			"		try (C c = new C()) {\n" +
			"			System.out.println(\"B::~B::Try\");\n" +
			"			throw new Exception (\"B::~B\");\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"			throw e;\n" +
			"	} 	\n" +
			"	}\n" +
			"}\n" +
			"class C implements AutoCloseable {\n" +
			"	public C () throws Exception {\n" +
			"		System.out.println(\"C::C\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"C::~C\");\n" +
			"		throw new Exception (\"C::~C\");\n" +
			"	} \n" +
			"}\n"
		},
		"Main\n" + 
		"A::A\n" + 
		"X::Try\n" + 
		"A::~A\n" + 
		"B::B\n" + 
		"A::~A::Try\n" + 
		"B::~B\n" + 
		"C::C\n" + 
		"B::~B::Try\n" + 
		"C::~C\n" + 
		"java.lang.Exception: B::~B\n" + 
		"Suppressed: java.lang.Exception: C::~C\n" + 
		"java.lang.Exception: A::~A\n" + 
		"Suppressed: java.lang.Exception: B::~B\n" + 
		"java.lang.Exception: X::Main\n" + 
		"Suppressed: java.lang.Exception: A::~A\n" + 
		"All done");
}
//ensure that it doesn't completely fail when using TWR and 1.5 mode
public void test049() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	this.runNegativeTest(
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
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	try(FileReader fileReader = new FileReader(file);) {\n" + 
		"	    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Resource specification not allowed here for source level below 1.7\n" + 
		"----------\n",
		null,
		true,
		options);
}
public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String [] args) {\n" +
			"		System.out.println(\"Main\");\n" +
			"		try (E e = E.CONST) {\n" +
			"			System.out.println(\"Outer try\");\n" +
			"		} catch (Exception e) {\n" +
			"			System.out.println(e);\n" +
			"			Throwable suppressed [] = e.getSuppressed();\n" +
			"			for (int i = 0; i < suppressed.length; ++i) {\n" +
			"				System.out.println(\"Suppressed: \" + suppressed[i]);\n" +
			"			}\n" +
			"		} finally {\n" +
			"			System.out.println(\"All done\");\n" +
			"		}\n" +
			"	}\n" +
			"}",
			"E.java",
			"public enum E implements AutoCloseable {\n" +
			"	CONST;\n" +
			"	private E () {\n" +
			"		System.out.println(\"E::E\");\n" +
			"	}\n" +
			"	public void close() throws Exception {\n" +
			"		System.out.println(\"E::~E\");\n" +
			"		throw new Exception (\"E::~E\");\n" +
			"	}\n" +
			"}"
		},
		"Main\n" + 
		"E::E\n" + 
		"Outer try\n" + 
		"E::~E\n" + 
		"java.lang.Exception: E::~E\n" + 
		"All done");
}
public void test051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
					"    public static void main(String[] args) throws Throwable {\n" +
					"        try (Test t = new Test()) {\n" +
					"            for (int i = 0; i < 10; i++) {\n" +
					"            }\n" +
					"\n" +
					"\n" +
					"        } \n" +
					"\n" +
					"        catch (Exception e) {\n" +
					"            StackTraceElement t = e.getStackTrace()[1];\n" +
					"            String file = t.getFileName();\n" +
					"            int line = t.getLineNumber();\n" +
					"            System.out.println(\"File = \" + file + \" \" + \"line = \" + line);\n" +
					"        }\n" +
					"    }\n" +
					"}\n" +
					"class Test implements AutoCloseable {\n" +
					"    public void close() throws Exception {\n" +
					"        throw new Exception();\n" +
					"    }\n" +
					"}\n"
		},
		"File = X.java line = 8");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348406
public void test052() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
					"    public static void main(String[] args) throws Throwable {\n" +
					"        try (Test t = new Test()) {\n" +
					"        } \n" +
					"    }\n" +
					"}\n" +
					"class Test {\n" +
					"    public void close() throws Exception {\n" +
					"        throw new Exception();\n" +
					"    }\n" +
					"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Test t = new Test()) {\n" + 
		"	     ^^^^^^^^^^^^^^^^^^^\n" + 
		"Resource specification not allowed here for source level below 1.7\n" + 
		"----------\n",
		null, 
		true,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348705
// Unhandled exception due to autoclose should be reported separately
public void test053() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y y = new Y()) { \n" +
			"			y.close();\n" +
			"			System.out.println();\n" +
			"		} catch (RuntimeException e) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements Managed {\n" +
			"	 public Y() throws CloneNotSupportedException {}\n" +
			"    public void close () throws ClassNotFoundException, java.io.IOException {\n" +
			"    }\n" +
			"}\n" +
			"interface Managed extends AutoCloseable {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y()) { \n" + 
		"	       ^\n" + 
		"Unhandled exception type ClassNotFoundException thrown by automatic close() invocation on y\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y()) { \n" + 
		"	       ^\n" + 
		"Unhandled exception type IOException thrown by automatic close() invocation on y\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y()) { \n" + 
		"	           ^^^^^^^\n" + 
		"Unhandled exception type CloneNotSupportedException\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	y.close();\n" + 
		"	^^^^^^^^^\n" + 
		"Unhandled exception type ClassNotFoundException\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 4)\n" + 
		"	y.close();\n" + 
		"	^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=348705
// Variant of the above, witness for https://bugs.eclipse.org/358827#c6
public void test053a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public void method1(){\n" +
			"		try (Y y = new Y()) { \n" +
			"			y.close();\n" +
			"			System.out.println();\n" +
			"		} catch (RuntimeException e) {\n" +
			"       } finally {\n" +
			"           System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Y implements Managed {\n" +
			"	 public Y() throws CloneNotSupportedException {}\n" +
			"    public void close () throws ClassNotFoundException, java.io.IOException {\n" +
			"    }\n" +
			"}\n" +
			"interface Managed extends AutoCloseable {}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y()) { \n" + 
		"	       ^\n" + 
		"Unhandled exception type ClassNotFoundException thrown by automatic close() invocation on y\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y()) { \n" + 
		"	       ^\n" + 
		"Unhandled exception type IOException thrown by automatic close() invocation on y\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 3)\n" + 
		"	try (Y y = new Y()) { \n" + 
		"	           ^^^^^^^\n" + 
		"Unhandled exception type CloneNotSupportedException\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 4)\n" + 
		"	y.close();\n" + 
		"	^^^^^^^^^\n" + 
		"Unhandled exception type ClassNotFoundException\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 4)\n" + 
		"	y.close();\n" + 
		"	^^^^^^^^^\n" + 
		"Unhandled exception type IOException\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862 (NPE when union type is used in the resource section.)
public void test054() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        try (Object | Integer res = null) {\n" +
			"        } catch (Exception e) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Object | Integer res = null) {\n" + 
		"	            ^\n" + 
		"Syntax error on token \"|\", . expected\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862 (NPE when union type is used in the resource section.)
public void test054a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    void foo() {\n" +
			"        try (Object.Integer res = null) {\n" +
			"        } catch (Exception e) {\n" +
			"        }\n" +
			"    }\n" +
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	try (Object.Integer res = null) {\n" + 
		"	     ^^^^^^^^^^^^^^\n" + 
		"Object.Integer cannot be resolved to a type\n" + 
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353535 (verify error with try with resources)
public void test055() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.ByteArrayInputStream;\n" +
			"import java.io.InputStream;\n" +
			"public class X {\n" +
			"public static void main(String[] args) throws Exception {\n" +
			"  int b;\n" +
			"  try (final InputStream in = new ByteArrayInputStream(new byte[] { 42 })) {\n" +
			"    b = in.read();\n" +
			"  }\n" +
			"  System.out.println(\"Done\");\n" +
			"}\n" +
			"}\n",
		},
		"Done");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353535 (verify error with try with resources)
public void test055a() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) throws Throwable {\n" +
			"        int tmp;\n" +
			"        try (A a = null) {\n" +
			"            try (A b = null) {\n" +
			"                tmp = 0;\n" +
			"            }\n" +
			"        }\n" +
			"        System.out.println(\"Done\");\n" +
			"    }\n" +
			"}\n" +
			"class A implements AutoCloseable {\n" +
			"    @Override\n" +
			"    public void close() {\n" +
			"    }\n" +
			"}\n",
		},
		"Done");
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without ever closing it.
public void test056() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
// not invoking any methods on FileReader, try to avoid necessary call to superclass() in the compiler
//			"        char[] in = new char[50];\n" +
//			"        fileReader.read(in);\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	FileReader fileReader = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Resource leak: 'fileReader' is never closed\n" +
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable and closes it but not protected by t-w-r nor regular try-finally
public void test056a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"		 fileReader.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ioex) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	FileReader fileReader = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Resource 'fileReader' should be managed by try-with-resource\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable and closes it properly in a finally block
public void test056b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        try {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } finally {\n" +
			"		     fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ioex) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable properly within try-with-resources.
public void test056c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        try (FileReader fileReader = new FileReader(file)) {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"		 }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ioex) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses two AutoCloseables (testing independent analysis)
// - one closeable may be unclosed at a conditional return
// - the other is only conditionally closed
public void test056d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean flag1, boolean flag2) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        char[] in = new char[50];\n" +
			"        FileReader fileReader1 = new FileReader(file);\n" +
			"        fileReader1.read(in);\n" +
			"        FileReader fileReader2 = new FileReader(file);\n" +
			"        fileReader2.read(in);\n" +
			"        if (flag1) {\n" +
			"            fileReader2.close();\n" +
			"            return;\n" +
			"        } else if (flag2) {\n" +
			"            fileReader2.close();\n" +
			"        }\n" +
			"        fileReader1.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(false, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 10)\n" + 
		"	FileReader fileReader2 = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^^\n" + 
		"Potential resource leak: 'fileReader2' may not be closed\n" +
		"----------\n" + 
		"2. ERROR in X.java (at line 14)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Resource leak: 'fileReader1' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
//Bug 349326 - [1.7] new warning for missing try-with-resources
//a method uses two AutoCloseables (testing independent analysis)
//- one closeable may be unclosed at a conditional return
//- the other is only conditionally closed
public void test056d_suppress() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean flag1, boolean flag2) throws IOException {\n" +
			"        @SuppressWarnings(\"resource\") File file = new File(\"somefile\"); // unnecessary suppress\n" +
			"        char[] in = new char[50];\n" +
			"        FileReader fileReader1 = new FileReader(file);\n" +
			"        fileReader1.read(in);\n" +
			"        @SuppressWarnings(\"resource\") FileReader fileReader2 = new FileReader(file); // useful suppress\n" +
			"        fileReader2.read(in);\n" +
			"        if (flag1) {\n" +
			"            fileReader2.close();\n" +
			"            return; // not suppressed\n" +
			"        } else if (flag2) {\n" +
			"            fileReader2.close();\n" +
			"        }\n" +
			"        fileReader1.close();\n" +
			"    }\n" +
			"    @SuppressWarnings(\"resource\") // useful suppress\n" +
			"    void bar() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(false, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 6)\n" + 
		"	@SuppressWarnings(\"resource\") File file = new File(\"somefile\"); // unnecessary suppress\n" + 
		"	                  ^^^^^^^^^^\n" + 
		"Unnecessary @SuppressWarnings(\"resource\")\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 14)\n" + 
		"	return; // not suppressed\n" + 
		"	^^^^^^^\n" + 
		"Resource leak: 'fileReader1' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// one method returns an AutoCleasble, a second method uses this object without ever closing it.
public void test056e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    FileReader getReader(String filename) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        return fileReader;\n" + 		// don't complain here, pass responsibility to caller
			"    }\n" +
			"    void foo() throws IOException {\n" +
			"        FileReader reader = getReader(\"somefile\");\n" +
			"        char[] in = new char[50];\n" +
			"        reader.read(in);\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 11)\n" + 
		"	FileReader reader = getReader(\"somefile\");\n" + 
		"	           ^^^^^^\n" + 
		"Resource leak: 'reader' is never closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method explicitly closes its AutoCloseable rather than using t-w-r
public void test056f() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = null;\n" +
			"        try {\n" +
			"            fileReader = new FileReader(file);\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } finally {\n" +
			"            fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	FileReader fileReader = null;\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Resource 'fileReader' should be managed by try-with-resource\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned
public void test056g() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        fileReader = new FileReader(file);\n" +
			"        fileReader.read(in);\n" +
			"        fileReader.close();\n" +
			"        fileReader = null;\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	fileReader = new FileReader(file);\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Resource leak: 'fileReader' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned after null-assigned
public void test056g2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        fileReader = null;\n" +
			"        fileReader = new FileReader(file);\n" + // don't complain again, fileReader is null, so nothing can leak here
			"        fileReader.read(in);\n" +
			"        fileReader.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	fileReader = null;\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Resource leak: 'fileReader' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// two AutoCloseables at different nesting levels (anonymous local type)
public void test056h() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        final File file = new File(\"somefile\");\n" +
			"        final FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        new Runnable() {\n public void run() {\n" +
			"            try {\n" +
			"                fileReader.close();\n" +
			"                FileReader localReader = new FileReader(file);\n" +
			"            } catch (IOException ex) { /* nop */ }\n" +
			"        }}.run();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	final FileReader fileReader = new FileReader(file);\n" + 
		"	                 ^^^^^^^^^^\n" + 
		"Potential resource leak: 'fileReader' may not be closed\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 14)\n" + 
		"	FileReader localReader = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^^\n" + 
		"Resource leak: 'localReader' is never closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean f1, boolean f2) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        if (f1) {\n" +
			"            FileReader fileReader = new FileReader(file); // err: not closed\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            while (true) {\n" +
			"                 FileReader loopReader = new FileReader(file); // don't warn, properly closed\n" +
			"                 loopReader.close();" +
			"                 break;\n" +
			"            }\n" +
			"        } else {\n" +
			"            FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" +
			"            if (f2)\n" +
			"                fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(true, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	FileReader fileReader = new FileReader(file); // err: not closed\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Resource leak: 'fileReader' is never closed\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 16)\n" + 
		"	FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Potential resource leak: 'fileReader' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" + 
			"import java.io.FileReader;\n" + 
			"import java.io.IOException;\n" + 
			"public class X {\n" +
			"    void foo(boolean f1, boolean f2) throws IOException {\n" + 
			"        File file = new File(\"somefile\");\n" + 
			"        if (f1) {\n" + 
			"            FileReader fileReader = new FileReader(file); // properly closed\n" + 
			"            char[] in = new char[50];\n" + 
			"            fileReader.read(in);\n" + 
			"            while (true) {\n" + 
			"                  fileReader.close();\n" + 
			"                  FileReader loopReader = new FileReader(file); // don't warn, properly closed\n" + 
			"                  loopReader.close();\n" + 
			"                  break;\n" + 
			"            }\n" + 
			"        } else {\n" + 
			"            FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" + 
			"            if (f2)\n" + 
			"                fileReader.close();\n" + 
			"        }\n" + 
			"    }\n" + 
			"    public static void main(String[] args) throws IOException {\n" + 
			"        new X().foo(true, true);\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 18)\n" + 
		"	FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Potential resource leak: 'fileReader' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        read(fileReader);\n" +
			"    }\n" +
			"    void read(FileReader reader) { }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	FileReader fileReader = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Potential resource leak: 'fileReader' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056jconditional() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean b) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        synchronized (b ? this : new X()) {\n" +
			"            new ReadDelegator(fileReader);\n" +
			"        }\n" +
			"    }\n" +
			"    class ReadDelegator { ReadDelegator(FileReader reader) { } }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	FileReader fileReader = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Potential resource leak: 'fileReader' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// many locals, some are AutoCloseable.
// Unfortunately analysis cannot respect how exception exits may affect ra3 and rb3,
// doing so would create false positives.
public void test056k() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        int i01, i02, i03, i04, i05, i06, i07, i08, i09,\n" +
			"            i11, i12, i13, i14, i15, i16, i17, i18, i19,\n" +
			"            i21, i22, i23, i24, i25, i26, i27, i28, i29,\n" +
			"            i31, i32, i33, i34, i35, i36, i37, i38, i39,\n" +
			"            i41, i42, i43, i44, i45, i46, i47, i48, i49;\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader ra1 = null, ra2 = null;\n" +
			"        try {\n" +
			"            ra1 = new FileReader(file);\n" +
			"            ra2 = new FileReader(file);\n" +
			"            FileReader ra3 = new FileReader(file);\n" +
			"            char[] in = new char[50];\n" +
			"            ra1.read(in);\n" +
			"            ra2.read(in);\n" +
			"            ra3.close();\n" +
			"        } finally {\n" +
			"            ra1.close();\n" +
			"        }\n" +
			"        int i51, i52, i53, i54, i55, i56, i57, i58, i59, i60;\n" + // beyond this point locals are analyzed using extraBits
			"        FileReader rb1 = null, rb2 = null;\n" +
			"        try {\n" +
			"            rb1 = new FileReader(file);\n" +
			"            rb2 = new FileReader(file);\n" +
			"            FileReader rb3 = new FileReader(file);\n" +
			"            char[] in = new char[50];\n" +
			"            rb1.read(in);\n" +
			"            rb2.read(in);\n" +
			"            rb3.close();\n" +
			"        } finally {\n" +
			"            rb1.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 12)\n" + 
		"	FileReader ra1 = null, ra2 = null;\n" + 
		"	           ^^^\n" + 
		"Resource 'ra1' should be managed by try-with-resource\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 15)\n" + 
		"	ra2 = new FileReader(file);\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Resource leak: 'ra2' is never closed\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 16)\n" + 
		"	FileReader ra3 = new FileReader(file);\n" + 
		"	           ^^^\n" + 
		"Resource 'ra3' should be managed by try-with-resource\n" +
		"----------\n" + 
		"4. ERROR in X.java (at line 25)\n" + 
		"	FileReader rb1 = null, rb2 = null;\n" + 
		"	           ^^^\n" + 
		"Resource 'rb1' should be managed by try-with-resource\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 28)\n" + 
		"	rb2 = new FileReader(file);\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Resource leak: 'rb2' is never closed\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 29)\n" + 
		"	FileReader rb3 = new FileReader(file);\n" + 
		"	           ^^^\n" + 
		"Resource 'rb3' should be managed by try-with-resource\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// various non-problems
public void test056l() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    X(FileReader r0) {}\n" + // don't complain against argument
			"    FileReader getReader() { return null; }\n" +
			"    void foo(FileReader r1) throws IOException {\n" +
			"        FileReader fileReader = getReader();\n" +
			"        if (fileReader == null)\n" +
			"            return;\n" + // don't complain, resource is actually null
			"        FileReader r3 = getReader();\n" +
			"        if (r3 == null)\n" +
			"            r3 = new FileReader(new File(\"absent\"));\n" + // don't complain, previous resource is actually null
			"        try {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            r1.read(in);\n" +
			"        } finally {\n" +
			"            fileReader.close();\n" +
			"            r3.close();\n" +  // the effect of this close() call might be spoiled by exception in fileReader.close() above, but we ignore exception exits in the analysis
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        FileReader r2 = new FileReader(new File(\"inexist\")); // only potential problem: ctor X below might close r2\n" +
			"        new X(r2).foo(new FileReader(new File(\"notthere\")));\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	FileReader fileReader = getReader();\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Resource 'fileReader' should be managed by try-with-resource\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 11)\n" + 
		"	FileReader r3 = getReader();\n" + 
		"	           ^^\n" + 
		"Resource 'r3' should be managed by try-with-resource\n" +
		"----------\n" + 
		"3. ERROR in X.java (at line 24)\n" + 
		"	FileReader r2 = new FileReader(new File(\"inexist\")); // only potential problem: ctor X below might close r2\n" + 
		"	           ^^\n" + 
		"Potential resource leak: 'r2' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested try with early exit
public void test056m() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() {\n" +
			"        File file = new File(\"somefile\");" +
			"        try {\n" +
			"            FileReader fileReader = new FileReader(file);\n" +
			"            try {\n" +
			"                char[] in = new char[50];\n" +
			"                if (fileReader.read(in)==0)\n" +
			"                    return;\n" +
			"            } finally {\n" +
			"		         fileReader.close();\n" +
			"            }\n" +
			"        } catch (IOException e) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested try should not interfere with earlier analysis.
public void test056n() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"import java.io.FileNotFoundException;\n" +
			"public class X {\n" +
			"    void foo(File someFile, char[] buf) throws IOException {\n" + 
			"		FileReader fr1 = new FileReader(someFile);\n" + 
			"		try {\n" + 
			"			fr1.read(buf);\n" + 
			"		} finally {\n" + 
			"			fr1.close();\n" + 
			"		}\n" + 
			"		try {\n" + 
			"			FileReader fr3 = new FileReader(someFile);\n" + 
			"			try {\n" + 
			"			} finally {\n" + 
			"				fr3.close();\n" + 
			"			}\n" + 
			"		} catch (IOException e) {\n" + 
			"		}\n" + 
			"	 }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo(new File(\"missing\"), new char[100]);\n" +
			"        } catch (FileNotFoundException e) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// if close is guarded by null check this should still be recognized as definitely closed
public void test056o() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"import java.io.FileNotFoundException;\n" +
			"public class X {\n" +
			"    void foo(File someFile, char[] buf) throws IOException {\n" + 
			"		FileReader fr1 = null;\n" + 
			"		try {\n" +
			"           fr1 = new FileReader(someFile);" + 
			"			fr1.read(buf);\n" + 
			"		} finally {\n" + 
			"			if (fr1 != null)\n" +
			"               try {\n" +
			"                   fr1.close();\n" +
			"               } catch (IOException e) { /*do nothing*/ }\n" + 
			"		}\n" + 
			"	 }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo(new File(\"missing\"), new char[100]);\n" +
			"        } catch (FileNotFoundException e) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without ever closing it, type from a type variable
public void test056p() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.Reader;\n" +
			"import java.io.IOException;\n" +
			"public abstract class X <T extends Reader> {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        T fileReader = newReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"    }\n" +
			"    abstract T newReader(File file) throws IOException;\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X<FileReader>() {\n" +
			"            FileReader newReader(File f) throws IOException { return new FileReader(f); }\n" +
			"        }.foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	T fileReader = newReader(file);\n" + 
		"	  ^^^^^^^^^^\n" + 
		"Resource leak: 'fileReader' is never closed\n" +
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// closed in dead code
public void test056q() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        if (2*2 == 4)\n" +
			"        	return;\n" +
			"        fileReader.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	FileReader fileReader = new FileReader(file);\n" + 
		"	           ^^^^^^^^^^\n" + 
		"Resource leak: 'fileReader' is never closed\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 10)\n" + 
		"	if (2*2 == 4)\n" + 
		"	    ^^^^^^^^\n" + 
		"Comparing identical expressions\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 12)\n" + 
		"	fileReader.close();\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// closed in dead code
public void test056r() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fr = new FileReader(file);\n" +
			"  		 Object b = null;\n" + 
			"        fr.close();\n" + 
			"        if (b != null) {\n" + 
			"            fr = new FileReader(file);\n" + 
			"            return;\n" + 
			"        } else {\n" + 
			"            System.out.print(42);\n" + 
			"        }\n" + 
			"        return;     // Should not complain about fr\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	if (b != null) {\n" + 
		"            fr = new FileReader(file);\n" + 
		"            return;\n" + 
		"        } else {\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 13)\n" + 
		"	} else {\n" + 
		"            System.out.print(42);\n" + 
		"        }\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource inside t-w-r is re-assigned, shouldn't even record an errorLocation
public void test056s() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" + 
			"import java.io.FileReader;\n" + 
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"    void foo() throws IOException {\n" + 
			"        File file = new File(\"somefile\");\n" + 
			"        try (FileReader fileReader = new FileReader(file);) {\n" + 
			"            char[] in = new char[50];\n" + 
			"            fileReader.read(in);\n" + 
			"            fileReader = new FileReader(file);  // debug here\n" + 
			"            fileReader.read(in);\n" + 
			"        }\n" + 
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" + 
			"        new X().foo();\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	fileReader = new FileReader(file);  // debug here\n" + 
		"	^^^^^^^^^^\n" + 
		"The resource fileReader of a try-with-resources statement cannot be assigned\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource is closed, dead code follows
public void test056t() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" + 
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"    void foo31() throws IOException {\n" + 
			"        FileReader reader = new FileReader(\"file\"); //warning\n" +
			"        if (reader != null) {\n" + 
			"            reader.close();\n" + 
			"        } else {\n" + 
			"            // nop\n" + 
			"        }\n" + 
			"    }\n" + 
			"    public static void main(String[] args) throws IOException {\n" + 
			"        new X().foo31();\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
		"	} else {\n" + 
		"            // nop\n" + 
		"        }\n" + 
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Dead code\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource is reassigned within t-w-r with different resource
// was initially broken due to https://bugs.eclipse.org/358827
public void test056u() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo() throws Exception {\n" +
			"        FileReader reader1 = new FileReader(\"file1\");\n" +
			"        FileReader reader2 = new FileReader(\"file2\");\n" +
			"        reader2 = reader1;// this disconnects reader 2\n" +
			"        try (FileReader reader3 = new FileReader(\"file3\")) {\n" +
			"            int ch;\n" +
			"            while ((ch = reader2.read()) != -1) {\n" +
			"                System.out.println(ch);\n" +
			"                reader1.read();\n" +
			"            }\n" +
			"            reader2 = reader1; // warning 1 regarding original reader1\n" + // this warning was missing
			"            reader2 = reader1; // warning 2 regarding original reader1\n" +
			"        } finally {\n" +
			"            if (reader2 != null) {\n" +
			"                reader2.close();\n" +
			"            } else {\n" +
			"                System.out.println();\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	FileReader reader2 = new FileReader(\"file2\");\n" + 
		"	           ^^^^^^^\n" + 
		"Resource leak: 'reader2' is never closed\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 13)\n" + 
		"	reader2 = reader1; // warning 1 regarding original reader1\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Resource leak: 'reader1' is not closed at this location\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 14)\n" + 
		"	reader2 = reader1; // warning 2 regarding original reader1\n" + 
		"	^^^^^^^^^^^^^^^^^\n" + 
		"Resource leak: 'reader1' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// scope-related pbs reported in https://bugs.eclipse.org/349326#c70 and https://bugs.eclipse.org/349326#c82  
public void test056v() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" + 
			"public class X {\n" + 
			"    boolean foo1() throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\");\n" + 
			"        try {\n" + 
			"            int ch;\n" + 
			"            while ((ch = reader.read()) != -1) {\n" + 
			"                System.out.println(ch);\n" + 
			"                reader.read();\n" + 
			"            }\n" + 
			"            if (ch > 10) {\n" + 
			"                return true;\n" + 
			"            }\n" + 
			"            return false;\n" + // return while resource from enclosing scope remains unclosed
			"        } finally {\n" + 
			"        }\n" + 
			"    }\n" + 
			"    void foo111() throws Exception {\n" + 
			"        FileReader reader111 = new FileReader(\"file2\");\n" + 
			"        try {\n" + 
			"            int ch;\n" + 
			"            while ((ch = reader111.read()) != -1) {\n" + 
			"                System.out.println(ch);\n" + 
			"                reader111.read();\n" + 
			"            }\n" + 
			"            return;\n" + // this shouldn't spoil the warning "should be managed with t-w-r" 
			"        } finally {\n" + 
			"            if (reader111 != null) {\n" + 
			"                reader111.close();\n" + 
			"            }\n" + 
			"        }\n" + 
			"    }\n" +
			"    void foo2() throws Exception {\n" + 
			"        FileReader reader2 = new FileReader(\"file\");\n" + 
			"        try {\n" + 
			"            int ch;\n" + 
			"            while ((ch = reader2.read()) != -1) {\n" + 
			"                System.out.println(ch);\n" + 
			"                reader2.read();\n" + 
			"            }\n" + 
			"            if (ch > 10) {\n" + 
			"                return;\n" + // potential leak
			"            }\n" + 
			"        } finally {\n" + 
			"        }\n" +
			"        reader2.close();\n" + // due to this close we don't say "never closed"
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	FileReader reader = new FileReader(\"file\");\n" + 
		"	           ^^^^^^\n" + 
		"Resource leak: 'reader' is never closed\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 19)\n" + 
		"	FileReader reader111 = new FileReader(\"file2\");\n" + 
		"	           ^^^^^^^^^\n" + 
		"Resource 'reader111' should be managed by try-with-resource\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 42)\n" + 
		"	return;\n" + 
		"	^^^^^^^\n" + 
		"Resource leak: 'reader2' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// end of method is dead end, but before we have both a close() and an early return
public void test056w() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" + 
			"public class X {\n" + 
			"    boolean foo1() throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\");\n" + 
			"        try {\n" + 
			"            int ch;\n" + 
			"            while ((ch = reader.read()) != -1) {\n" + 
			"                System.out.println(ch);\n" + 
			"                reader.read();\n" + 
			"            }\n" + 
			"            if (ch > 10) {\n" +
			"				 reader.close();\n" + 
			"                return true;\n" + 
			"            }\n" + 
			"            return false;\n" + 
			"        } finally {\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 15)\n" + 
		"	return false;\n" + 
		"	^^^^^^^^^^^^^\n" + 
		"Resource leak: 'reader' is not closed at this location\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// different early exits, if no close seen report as definitely unclosed
public void test056x() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo31(boolean b) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        if (b) {\n" +
			"            reader.close();\n" +
			"        } else {\n" +
			"            return; // warning\n" +
			"        }\n" +
			"    }\n" +
			"    void foo32(boolean b) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\"); // warn here\n" +
			"        return;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	return; // warning\n" +
		"	^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	FileReader reader = new FileReader(\"file\"); // warn here\n" +
		"	           ^^^^^^\n" +
		"Resource leak: 'reader' is never closed\n" +
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested method passes the resource to outside code
public void test056y() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo31(boolean b) throws Exception {\n" +
			"        final FileReader reader31 = new FileReader(\"file\");\n" +
			"        new Runnable() {\n" +
			"            public void run() {\n" +
			"                foo18(reader31);\n" +
			"            }\n" +
			"        }.run();\n" +
			"    }\n" +
			"    void foo18(FileReader r18) {\n" +
			"        // could theoretically close r18;\n" +
			"    }\n" +
			"    abstract class ResourceProvider {\n" +
			"        abstract FileReader provide();" +
			"    }\n" +
			"    ResourceProvider provider;" +
			"    void foo23() throws Exception {\n" +
			"        final FileReader reader23 = new FileReader(\"file\");\n" +
			"        provider = new ResourceProvider() {\n" +
			"            public FileReader provide() {\n" +
			"                return reader23;\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	final FileReader reader31 = new FileReader(\"file\");\n" + 
		"	                 ^^^^^^^^\n" + 
		"Potential resource leak: 'reader31' may not be closed\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 17)\n" + 
		"	final FileReader reader23 = new FileReader(\"file\");\n" + 
		"	                 ^^^^^^^^\n" + 
		"Potential resource leak: 'reader23' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource assigned to second local and is (potentially) closed on the latter
public void test056z() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo17() throws Exception {\n" +
			"        FileReader reader17 = new FileReader(\"file\");\n" +
			"        final FileReader readerCopy = reader17;\n" +
			"        readerCopy.close();\n" +
			"    }\n" +
			"    void foo17a() throws Exception {\n" +
			"        FileReader reader17a = new FileReader(\"file\");\n" +
			"        FileReader readerCopya;" +
			"		 readerCopya = reader17a;\n" +
			"        bar(readerCopya);\n" + // potentially closes
			"    }\n" +
			"    void bar(FileReader r) {}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	FileReader reader17a = new FileReader(\"file\");\n" + 
		"	           ^^^^^^^^^\n" + 
		"Potential resource leak: 'reader17a' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// multiple early exists from nested scopes (always closed) 
public void test056zz() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo16() throws Exception {\n" +
			"        FileReader reader16 = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            reader16.close();\n " +
			"            return;\n" +
			"        } catch (RuntimeException re) {\n" +
			"            return;\n" +
			"        } catch (Error e) {\n" +
			"            return;\n" +
			"        } finally {\n" +
			"            reader16.close();\n " +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	FileReader reader16 = new FileReader(\"file\");\n" + 
		"	           ^^^^^^^^\n" + 
		"Resource 'reader16' should be managed by try-with-resource\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// multiple early exists from nested scopes (never closed) 
public void test056zzz() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo16() throws Exception {\n" +
			"        FileReader reader16 = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            return;\n" +
			"        } catch (RuntimeException re) {\n" +
			"            return;\n" +
			"        } catch (Error e) {\n" +
			"            return;\n" +
			"        } finally {\n" +
			"            System.out.println();\n " +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	FileReader reader16 = new FileReader(\"file\");\n" + 
		"	           ^^^^^^^^\n" + 
		"Resource leak: 'reader16' is never closed\n" + 
		"----------\n",
		null,
		true,
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// explicit throw is a true method exit here
public void test056throw1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2(boolean a, boolean b, boolean c) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        if(a)\n" +
			"            throw new Exception();    //warning 1\n" +
			"        else if (b)\n" +
			"            reader.close();\n" +
			"        else if(c)\n" +
			"            throw new Exception();    //warning 2\n" +
			"        reader.close();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" +
		"	throw new Exception();    //warning 1\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	throw new Exception();    //warning 2\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n",
		null,
		true,
		options);	
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// close() within finally provides protection for throw
public void test056throw2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo1() throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" + 
			"        try {\n" + 
			"            reader.read();\n" + 
			"            return;\n" + 
			"        } catch (Exception e) {\n" + 
			"            throw new Exception();\n" + 
			"        } finally {\n" + 
			"            reader.close();\n" + 
			"        }\n" + 
			"    }\n" + 
			"\n" + 
			"    void foo2() throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" + 
			"        try {\n" + 
			"            reader.read();\n" + 
			"            throw new Exception(); // should not warn here\n" + 
			"        } catch (Exception e) {\n" + 
			"            throw new Exception();\n" + 
			"        } finally {\n" + 
			"            reader.close();\n" + 
			"        }\n" + 
			"    }\n" + 
			"\n" + 
			"    void foo3() throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" + 
			"        try {\n" + 
			"            reader.read();\n" + 
			"            throw new Exception();\n" + 
			"        } finally {\n" + 
			"            reader.close();\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
		"	           ^^^^^^\n" +
		"Resource 'reader' should be managed by try-with-resource\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
		"	           ^^^^^^\n" +
		"Resource 'reader' should be managed by try-with-resource\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 28)\n" +
		"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
		"	           ^^^^^^\n" +
		"Resource 'reader' should be managed by try-with-resource\n" +
		"----------\n",
		null,
		true,
		options);	
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// close() nested within finally provides protection for throw
public void test056throw3() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2x() throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" + 
			"        try {\n" + 
			"            reader.read();\n" + 
			"            throw new Exception(); // should not warn here\n" + 
			"        } catch (Exception e) {\n" + 
			"            throw new Exception();\n" + 
			"        } finally {\n" +
			"            if (reader != null)\n" +
			"                 try {\n" + 
			"                     reader.close();\n" +
			"                 } catch (java.io.IOException io) {}\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" + 
		"	           ^^^^^^\n" + 
		"Resource 'reader' should be managed by try-with-resource\n" + 
		"----------\n",
		null,
		true,
		options);	
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// additional boolean should shed doubt on whether we reach the close() call
public void test056throw4() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2x(boolean b) throws Exception {\n" + 
			"        FileReader reader = new FileReader(\"file\");\n" + 
			"        try {\n" + 
			"            reader.read();\n" + 
			"            throw new Exception(); // should warn here\n" + 
			"        } catch (Exception e) {\n" + 
			"            throw new Exception(); // should warn here\n" + 
			"        } finally {\n" +
			"            if (reader != null && b)\n" + // this condition is too strong to protect reader
			"                 try {\n" + 
			"                     reader.close();\n" +
			"                 } catch (java.io.IOException io) {}\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	throw new Exception(); // should warn here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	throw new Exception(); // should warn here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n",
		null,
		true,
		options);	
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// similar to test056throw3() but indirectly calling close(), so doubts remain.
public void test056throw5() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2x() throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            reader.read();\n" +
			"            throw new Exception(); // should warn 'may not' here\n" +
			"        } catch (Exception e) {\n" +
			"            throw new Exception(); // should warn 'may not' here\n" +
			"        } finally {\n" +
			"            doClose(reader);\n" +
			"        }\n" +
			"    }\n" +
			"    void doClose(FileReader r) { try { r.close(); } catch (java.io.IOException ex) {}}\n" + 
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	throw new Exception(); // should warn \'may not\' here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	throw new Exception(); // should warn \'may not\' here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n",
		null,
		true,
		options);	
}
public static Class testClass() {
	return TryWithResourcesStatementTest.class;
}
}
