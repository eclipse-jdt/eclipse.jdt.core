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
import junit.framework.TestSuite;

public class AssignmentTest extends AbstractRegressionTest {
	
public AssignmentTest(String name) {
	super(name);
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
	return options;
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new AssignmentTest("test221"));
		return new RegressionTestSetup(ts, COMPLIANCE_1_4);
	}
	return setupSuite(testClass());
}

/*
 * no effect assignment bug
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=27235
 */
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    int i;	\n" +
			"    X(int j) {	\n" +
			"    	i = j;	\n" +
			"    }	\n" +
			"    X() {	\n" +
			"    }	\n" +
			"    class B extends X {	\n" +
			"        B() {	\n" +
			"            this.i = X.this.i;	\n" +
			"        }	\n" +
			"    }	\n" +
			"    public static void main(String[] args) {	\n" +
			"        X a = new X(3);	\n" +
			"        System.out.print(a.i + \" \");	\n" +
			"        System.out.print(a.new B().i);	\n" +
			"	}	\n" +
			"}	\n",
		},
		"3 3");
}

public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int a;	\n" + 
			"	X next;	\n" + 
			"	public void foo(int arg){	\n" + 
			"	\n" + 
			"		zork = zork;	\n" +
			"		arg = zork;	\n" +
			"	\n" + 
			"		arg = arg;  // noop	\n" + 
			"		a = a;  // noop	\n" + 
			"		this.next = this.next; // noop	\n" + 
			"		this.next = next; // noop	\n" + 
			"	\n" + 
			"		next.a = next.a; // could raise NPE	\n" + 
			"		this.next.next.a = next.next.a; // could raise NPE	\n" + 
			"		a = next.a; // could raise NPE	\n" + 
			"		this. a = next.a; 	\n" + 
			"	}	\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	zork = zork;	\n" + 
		"	^^^^\n" + 
		"zork cannot be resolved\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	zork = zork;	\n" + 
		"	       ^^^^\n" + 
		"zork cannot be resolved\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	arg = zork;	\n" + 
		"	      ^^^^\n" + 
		"zork cannot be resolved\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	arg = arg;  // noop	\n" + 
		"	^^^^^^^^^\n" + 
		"The assignment to variable arg has no effect\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 10)\n" + 
		"	a = a;  // noop	\n" + 
		"	^^^^^\n" + 
		"The assignment to variable a has no effect\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 11)\n" + 
		"	this.next = this.next; // noop	\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable next has no effect\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 12)\n" + 
		"	this.next = next; // noop	\n" + 
		"	^^^^^^^^^^^^^^^^\n" + 
		"The assignment to variable next has no effect\n" + 
		"----------\n");
}
/*
 * check null/non-null reference diagnosis
 */
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	X foo(X x) {\n" + 
			"		x.foo(null);\n" + 
			"		if (x == null) {\n" + 
			"			x.foo(null);\n" + 
			"		}\n" + 
			"		return this;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	if (x == null) {\n" + 
		"	    ^\n" + 
		"The variable x cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	x.foo(null);\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	X foo(X x) {\n" + 
			"		x.foo(null); // 0\n" + 
			"		if (x != null) { // 1\n" + 
			"			if (x == null) { // 2\n" + 
			"				x.foo(null); // 3\n" + 
			"			} else if (x instanceof X) { // 4\n" + 
			"				x.foo(null); // 5 \n" + 
			"			} else if (x != null) { // 6\n" + 
			"				x.foo(null); // 7\n" + 
			"			}\n" + 
			"			x.foo(null); // 8\n" + 
			"		}\n" + 
			"		return this;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	if (x != null) { // 1\n" + 
		"	    ^\n" + 
		"The variable x cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	if (x == null) { // 2\n" + 
		"	    ^\n" + 
		"The variable x cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 6)\n" + 
		"	x.foo(null); // 3\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	} else if (x != null) { // 6\n" + 
		"	           ^\n" + 
		"The variable x cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n");
}
public void test005() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(Class c) {\n" + 
			"		if (c.isArray() ) {\n" + 
			"		} else if (c == java.lang.String.class ) {\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x) {\n" + 
			"		if (x == this)\n" + 
			"			return;\n" + 
			"		x.foo(this);\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x, X x2) {\n" + 
			"		if (x != null)\n" + 
			"			return;\n" + 
			"		x = x2;\n" + 
			"		if (x == null) {\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x, X x2) {\n" + 
			"		if (x != null)\n" + 
			"			return;\n" + 
			"		try {\n" + 
			"			x = x2;\n" + 
			"		} catch(Exception e) {}\n" + 
			"		if (x == null) {\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
// TODO (philippe) reenable once fixed
public void _test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	boolean check(String name) { return true; }\n" + 
			"	Class bar(String name) throws ClassNotFoundException { return null; }\n" + 
			"	File baz(String name) { return null; }\n" + 
			"	\n" + 
			"	public Class foo(String name, boolean resolve) throws ClassNotFoundException {\n" + 
			"			\n" + 
			"		Class c = bar(name);\n" + 
			"		if (c != null)\n" + 
			"			return c;\n" + 
			"		if (check(name)) {\n" + 
			"			try {\n" + 
			"				c= bar(name);\n" + 
			"				return c;\n" + 
			"			} catch (ClassNotFoundException e) {\n" + 
			"				// keep searching\n" + 
			"				// only path to here left c unassigned from try block, means it was assumed to be null\n" + 
			"			}\n" + 
			"		}\n" + 
			"		if (c == null) {// should complain: c can only be null\n" + 
			"			File file= baz(name);\n" + 
			"			if (file == null)\n" + 
			"				throw new ClassNotFoundException();\n" + 
			"		}\n" + 
			"		return c;\n" + 
			"	}\n" + 
			"\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 22)\n" + 
		"	if (c == null) {// should complain: c can only be null\n" + 
		"	    ^\n" + 
		"The variable c can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"	X itself() { return this; }\n" + 
			"\n" + 
			"	void bar() {\n" + 
			"		X itself = this.itself();\n" + 
			"		if (this == itself) {\n" + 
			"			System.out.println(itself.toString()); //1\n" + 
			"		} else {\n" + 
			"			System.out.println(itself.toString()); //2\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"	X itself() { return this; }\n" + 
			"\n" + 
			"	void bar() {\n" + 
			"		X itself = this.itself();\n" + 
			"		if (this == itself) {\n" + 
			"			X other = (X)itself;\n" + 
			"			if (other != null) {\n" + 
			"			}\n" + 
			"			if (other == null) {\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	if (other != null) {\n" + 
		"	    ^^^^^\n" + 
		"The variable other cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n");
}
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object o = null;\n" + 
			"		do {\n" + 
			"			if (o == null) return;\n" + 
			"			o = bar();\n" + 
			"		} while (true);\n" + 
			"	}\n" + 
			"	X bar() { \n" + 
			"		return null; \n" + 
			"	}\n" + 
			"}",
		},
		"");
}
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x) {\n" + 
			"		if (x == this) {\n" + 
			"			if (x == null) {\n" + 
			"				x.foo(this);\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	if (x == null) {\n" + 
		"	    ^\n" + 
		"The variable x cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	x.foo(this);\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x) {\n" + 
			"		x = null;\n" + 
			"		try {\n" + 
			"			x = this;\n" + 
			"		} finally {\n" + 
			"			if (x != null) {\n" + 
			"				x.foo(null);\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    void foo() {\n" + 
			"       Object o = null;\n" + 
			"       int i = 1;\n" + 
			"       switch (i) {\n" + 
			"          case 1:\n" + 
			"             o = new Object();\n" + 
			"             break;\n" + 
			"       }\n" + 
			"       if (o != null)\n" + 
			"            o.toString();\n" + 
			"    }\n" + 
			"}\n",
		},
		"");
}
public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x) {\n" + 
			"		x = null;\n" + 
			"		try {\n" + 
			"			x = null;\n" + 
			"		} finally {\n" + 
			"			if (x != null) {\n" + 
			"				x.foo(null);\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	x = null;\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	if (x != null) {\n" + 
		"	    ^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo(X x) {\n" + 
			"		x = this;\n" + 
			"		try {\n" + 
			"			x = null;\n" + 
			"		} finally {\n" + 
			"			if (x == null) {\n" + 
			"				x.foo(null);\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (x == null) {\n" + 
		"	    ^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	x.foo(null);\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object o = null;\n" + 
			"		do {\n" + 
			"			if (o != null) return;\n" + 
			"			o = null;\n" + 
			"		} while (true);\n" + 
			"	}\n" + 
			"	X bar() { \n" + 
			"		return null; \n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\r\n" + 
		"	if (o != null) return;\r\n" + 
		"	    ^\n" + 
		"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\r\n" + 
		"	o = null;\r\n" + 
		"	^\n" + 
		"The variable o can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static final char[] replaceOnCopy(\n" + 
			"		char[] array,\n" + 
			"		char toBeReplaced,\n" + 
			"		char replacementChar) {\n" + 
			"		\n" + 
			"		char[] result = null;\n" + 
			"		for (int i = 0, length = array.length; i < length; i++) {\n" + 
			"			char c = array[i];\n" + 
			"			if (c == toBeReplaced) {\n" + 
			"				if (result == null) {\n" + 
			"					result = new char[length];\n" + 
			"					System.arraycopy(array, 0, result, 0, i);\n" + 
			"				}\n" + 
			"				result[i] = replacementChar;\n" + 
			"			} else if (result != null) {\n" + 
			"				result[i] = c;\n" + 
			"			}\n" + 
			"		}\n" + 
			"		if (result == null) return array;\n" + 
			"		return result;\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test020() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		final int v;\n" + 
			"		for (int i = 0; i < 10; i++) {\n" + 
			"			v = i;\n" + 
			"		}\n" + 
			"		v = 0;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	v = i;\n" + 
		"	^\n" + 
		"The final local variable v may already have been assigned\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	v = 0;\n" + 
		"	^\n" + 
		"The final local variable v may already have been assigned\n" + 
		"----------\n");
}
public void test021() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int kind;\n" + 
			"	X parent;\n" + 
			"	Object[] foo() { return null; }\n" + 
			"	private void findTypeParameters(X scope) {\n" + 
			"		Object[] typeParameters = null;\n" + 
			"		while (scope != null) {\n" + 
			"			typeParameters = null;\n" + 
			"			switch (scope.kind) {\n" + 
			"				case 0 :\n" + 
			"					typeParameters = foo();\n" + 
			"					break;\n" + 
			"				case 1 :\n" + 
			"					typeParameters = foo();\n" + 
			"					break;\n" + 
			"				case 2 :\n" + 
			"					return;\n" + 
			"			}\n" + 
			"			if(typeParameters != null) {\n" + 
			"				foo();\n" + 
			"			}\n" + 
			"			scope = scope.parent;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test022() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	boolean bool() { return true; }\n" + 
			"	void doSomething() {}\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object progressJob = null;\n" + 
			"		while (bool()) {\n" + 
			"			if (bool()) {\n" + 
			"				if (progressJob != null)\n" + 
			"					progressJob = null;\n" + 
			"				doSomething();\n" + 
			"			}\n" + 
			"			try {\n" + 
			"				if (progressJob == null) {\n" + 
			"					progressJob = new Object();\n" + 
			"				}\n" + 
			"			} finally {\n" + 
			"				doSomething();\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"");
}
public void test023() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"	void foo() {\n" + 
			"		Object o = new Object();\n" + 
			"		while (this != null) {\n" + 
			"			try {\n" + 
			"				o = null;\n" + 
			"				break;\n" + 
			"			} finally {\n" + 
			"				o = new Object();\n" + 
			"			}\n" + 
			"		}\n" + 
			"		if (o == null) return;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 13)\n" + 
		"	if (o == null) return;\n" + 
		"	    ^\n" + 
		"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n");
}
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	boolean bool() { return true; }\n" + 
			"	void doSomething() {}\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object progressJob = null;\n" + 
			"		while (bool()) {\n" + 
			"			if (progressJob != null)\n" + 
			"				progressJob = null;\n" + 
			"			doSomething();\n" + 
			"			try {\n" + 
			"				if (progressJob == null) {\n" + 
			"					progressJob = new Object();\n" + 
			"				}\n" + 
			"			} finally {\n" + 
			"				doSomething();\n" + 
			"			}\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 13)\n" + 
		"	if (progressJob == null) {\n" + 
		"	    ^^^^^^^^^^^\n" + 
		"The variable progressJob can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
public void test025() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object o;\n" + 
			"		try {\n" + 
			"			o = null;\n" + 
			"		} finally {\n" + 
			"			o = new Object();\n" + 
			"		}\n" + 
			"		if (o == null) return;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	if (o == null) return;\n" + 
		"	    ^\n" + 
		"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n");
}
// TODO (philippe) reenable once fixed
public void _test026() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object o;\n" + 
			"		try {\n" + 
			"			o = null;\n" + 
			"		} finally {\n" + 
			"			if (args == null) o = new Object();\n" + 
			"		}\n" + 
			"		if (o == null) System.out.println(\"SUCCESS\");\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
}
// TODO (philippe) reenable once fixed
public void _test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	boolean b;\n" + 
			"	void foo() {\n" + 
			"		Object o = null;\n" + 
			"		while (b) {\n" + 
			"			try {\n" + 
			"				o = null;\n" + 
			"			} finally {\n" + 
			"				if (o == null) \n" + 
			"					o = new Object();\n" + 
			"			}\n" + 
			"		}\n" + 
			"		if (o == null) return;\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
// TODO (philippe) reenable once fixed
public void _test028() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	boolean b;\n" + 
			"	void foo() {\n" + 
			"		Object o = null;\n" + 
			"		while (b) {\n" + 
			"			try {\n" + 
			"				o = null;\n" + 
			"				break;\n" + 
			"			} finally {\n" + 
			"				if (o == null) \n" + 
			"					o = new Object();\n" + 
			"			}\n" + 
			"		}\n" + 
			"		if (o == null) return;\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		Object o = null;\n" + 
			"		int i = 0;\n" + 
			"		while (i++ < 2) {\n" + 
			"			try {\n" + 
			"				if (i == 2) return;\n" + 
			"				o = null;\n" + 
			"			} finally {\n" + 
			"				if (i == 2) System.out.println(o);\n" + 
			"				if (o == null) \n" + 
			"					o = \"SUCCESS\";\n" + 
			"			}\n" + 
			"		}\n" + 
			"		if (o == null) return;\n" + 
			"	}\n" + 
			"}\n",
		},
		"SUCCESS");
}
public void test030() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object a = null;\n" + 
			"		while (true) {\n" + 
			"			a = null;\n" + 
			"			if (a == null) {\n" + 
			"				System.out.println();\n" + 
			"			}\n" + 
			"			a = new Object();\n" + 
			"			break;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (a == null) {\n" + 
		"	    ^\n" + 
		"The variable a can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n");
}
// TODO (philippe) reenable once fixed
public void _test031() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		Object a = null;\n" + 
			"		while (true) {\n" + 
			"			a = null;\n" + 
			"			if (a == null) {\n" + 
			"				System.out.println();\n" + 
			"			}\n" + 
			"			a = new Object();\n" + 
			"			break;\n" + 
			"		}\n" + 
			"		if (a == null) {\n" + 
			"			System.out.println();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	if (a == null) {\n" + 
		"	    ^\n" + 
		"The variable a can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 13)\n" + 
		"	if (a == null) {\n" + 
		"	    ^\n" + 
		"The variable a cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n");
}
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		Object o1 = this;\n" + 
			"		Object o3;\n" + 
			"		while (o1 != null && (o3 = o1) != null) {\n" + 
			"			o1 = o3;\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
public void test033() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	\n" + 
			"	void foo() {\n" + 
			"		String a,b;\n" + 
			"		do{\n" + 
			"		   a=\"Hello \";\n" + 
			"		}while(a!=null);\n" + 
			"				\n" + 
			"		if(a!=null)\n" + 
			"		{\n" + 
			"		   b=\"World!\";\n" + 
			"		}\n" + 
			"		System.out.println(a+b);\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	if(a!=null)\n" + 
		"	   ^\n" + 
		"The variable a cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 13)\n" + 
		"	System.out.println(a+b);\n" + 
		"	                     ^\n" + 
		"The local variable b may not have been initialized\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84215
//TODO (philippe) should move to InitializationTest suite
public void test034() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public final class X \n" + 
			"{\n" + 
			"	public static String vdg;\n" + 
			"	public static final String aa = null;\n" + 
			"	public static final int a = 14;\n" + 
			"	public static final int b = 3;\n" + 
			"	private static final int c = 12;\n" + 
			"	private static final int d = 2; \n" + 
			"	private static final int e = 3; \n" + 
			"	private static final int f = 34; \n" + 
			"	private static final int g = 35; \n" + 
			"	private static final int h = 36; \n" + 
			"	private static final int j = 4;\n" + 
			"	private static final int k = 1;\n" + 
			"	public static final int aba = 1;\n" + 
			"	public static final int as = 11;\n" + 
			"	public static final int ad = 12;\n" + 
			"	public static final int af = 13;\n" + 
			"	public static final int ag = 2;\n" + 
			"	public static final int ah = 21;\n" + 
			"	public static final int aj = 22;\n" + 
			"	public static final int ak = 3;\n" + 
			"	public static final String aaad = null;\n" + 
			"	public static final int aaaf = 1;\n" + 
			"	public static final int aaag = 2;\n" + 
			"	public static final int aaha = 2;\n" + 
			"	static int cxvvb = 1;\n" + 
			"	static int z = a;\n" + 
			"	String asdff;\n" + 
			"	public static String ppfp;\n" + 
			"	public static int ppfpged;\n" + 
			"	boolean asfadf;\n" + 
			"	boolean cbxbx;\n" + 
			"	private static long tyt, rrky;\n" + 
			"	private static int dgjt, ykjr6y;\n" + 
			"	private static final int krykr = 1;\n" + 
			"	protected static int rykr5;\n" + 
			"	protected static int dhfg;\n" + 
			"	private static int dthj;\n" + 
			"	private static int fkffy;\n" + 
			"	private static String fhfy;\n" + 
			"	protected static String fhmf;\n" + 
			"	protected String ryur6;\n" + 
			"	protected String dhdthd;\n" + 
			"	protected String dth5;\n" + 
			"	protected String kfyk;\n" + 
			"	private String ntd;\n" + 
			"	public int asdasdads;\n" + 
			"	public static final int dntdr = 7;\n" + 
			"	public static final int asys = 1;\n" + 
			"	public static final int djd5rwas = 11;\n" + 
			"	public static final int dhds45rjd = 12;\n" + 
			"	public static final int srws4jd = 13;\n" + 
			"	public static final int s4ts = 2;\n" + 
			"	public static final int dshes4 = 21;\n" + 
			"	public static final int drthed56u = 22;\n" + 
			"	public static final int drtye45 = 23;\n" + 
			"	public static final int xxbxrb = 3;\n" + 
			"	public static final int xfbxr = 31;\n" + 
			"	public static final int asgw4y = 32;\n" + 
			"	public static final int hdtrhs5r = 33;\n" + 
			"	public static final int dshsh = 34;\n" + 
			"	public static final int ds45yuwsuy = 4;\n" + 
			"	public static final int astgs45rys = 5;\n" + 
			"	public static final int srgs4y = 6;\n" + 
			"	public static final int srgsryw45 = -6;\n" + 
			"	public static final int srgdtgjd45ry = -7;\n" + 
			"	public static final int srdjs43t = 1;\n" + 
			"	public static final int sedteued5y = 2;\n" + 
			"	public static int jrfd6u;\n" + 
			"	public static int udf56u;\n" + 
			"	private String jf6tu;\n" + 
			"	private String jf6tud;\n" + 
			"	String bsrh;\n" + 
			"	protected X(String a)\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private long sfhdsrhs;\n" + 
			"	private boolean qaafasdfs;\n" + 
			"	private int sdgsa;\n" + 
			"	private long dgse4;\n" + 
			"	long sgrdsrg;\n" + 
			"	public void gdsthsr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private int hsrhs;\n" + 
			"	private void hsrhsdsh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private String dsfhshsr;\n" + 
			"	protected void sfhsh4rsrh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected void shsrhsh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected void sfhstuje56u()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	public void dhdrt6u()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	public void hdtue56u()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	private void htdws4()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	String mfmgf;\n" + 
			"	String mgdmd;\n" + 
			"	String mdsrh;\n" + 
			"	String nmdr;\n" + 
			"	private void oyioyio()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected static long oyioyreye()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"	protected static long etueierh()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"	protected static void sdfgsgs()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	protected static void fhsrhsrh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	long dcggsdg;\n" + 
			"	int ssssssgsfh;\n" + 
			"	long ssssssgae;\n" + 
			"	long ssssssfaseg;\n" + 
			"	public void zzzdged()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	String t;\n" + 
			"	protected void xxxxxcbsg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	\n" + 
			"	public void vdg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private int[] fffcvffffffasdfaef;\n" + 
			"	private int[] fffcffffffasdfaef;\n" + 
			"	private long[] ffcvfffffffasdfaef;\n" + 
			"	private int fffffghffffasdfaef; \n" + 
			"	private int fffffdffffasdfaef; \n" + 
			"	private String ffafffffffasdfaef;\n" + 
			"	\n" + 
			"	private void fffffffffasdfaef()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private boolean aaaadgasrg;\n" + 
			"	private void ddddgaergnj()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void aaaadgaeg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void aaaaaaefadfgh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void addddddddafge()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	static boolean aaaaaaaefae;\n" + 
			"	protected void aaaaaaefaef()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void ggggseae()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static void ggggggsgsrg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static synchronized void ggggggfsfgsr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void aaaaaadgaeg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void aaaaadgaerg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void bbbbbbsfryghs()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void bfbbbbbbfssreg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void bbbbbbfssfb()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void bbbbbbfssb()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private void bbbbfdssb()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	boolean dggggggdsg;\n" + 
			"\n" + 
			"	public void hdfhdr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void dhdrtdrs()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void dghdthtdhd()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void dhdhdtdh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private void fddhdsh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private boolean sdffgsdg()\n" + 
			"	{\n" + 
			"		return true;\n" + 
			"	}\n" + 
			"			\n" + 
			"	private static boolean sdgsdg()\n" + 
			"	{\n" + 
			"		return false;\n" + 
			"	}\n" + 
			"	\n" + 
			"	protected static final void sfdgsg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	static int[] fghtys;\n" + 
			"\n" + 
			"	protected static final int sdsst = 1;\n" + 
			"	private static X asdfahnr;\n" + 
			"	private static int ssdsdbrtyrtdfhd, ssdsrtyrdbdfhd;\n" + 
			"	protected static int ssdsrtydbdfhd, ssdsrtydffbdfhd;\n" + 
			"	protected static int ssdrtyhrtysdbdfhd, ssyeghdsdbdfhd;\n" + 
			"	private static int ssdsdrtybdfhd, ssdsdehebdfhd;\n" + 
			"	protected static int ssdthrtsdbdfhd, ssdshethetdbdfhd;\n" + 
			"	private static String sstrdrfhdsdbdfhd;\n" + 
			"	protected static int ssdsdbdfhd, ssdsdethbdfhd;\n" + 
			"	private static long ssdshdfhchddbdfhd;\n" + 
			"	private static long ssdsdvbbdfhd;\n" + 
			"	\n" + 
			"	\n" + 
			"	protected static long ssdsdbdfhd()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static long sdgsrsbsf()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static void sfgsfgssghr()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	protected static String sgsgsrg()\n" + 
			"	{\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static void sdgshsdygra()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static String sdfsdfs()\n" + 
			"	{\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	static boolean ryweyer;\n" + 
			"\n" + 
			"	protected static void adfadfaghsfh()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	protected static void ghasghasrg()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	private static void aadfadfaf()\n" + 
			"	{\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static void aadfadf()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private static int fgsfhwr()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static int gdfgfgrfg()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static int asdfsfs()\n" + 
			"	{\n" + 
			"		return 0;\n" + 
			"	}\n" + 
			"\n" + 
			"	protected static String sdgs;\n" + 
			"	protected static String sdfsh4e;\n" + 
			"	protected static final int gsregs = 0;\n" + 
			"	\n" + 
			"	protected static String sgsgsd()\n" + 
			"	{\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	private byte[] sdhqtgwsrh(String rsName, int id)\n" + 
			"	{\n" + 
			"		String rs = null;\n" + 
			"		try\n" + 
			"		{\n" + 
			"			rs = \"\";\n" + 
			"			return null;\n" + 
			"		}\n" + 
			"		catch (Exception ex)\n" + 
			"		{\n" + 
			"		}\n" + 
			"		finally\n" + 
			"		{\n" + 
			"			if (rs != null)\n" + 
			"			{\n" + 
			"				try\n" + 
			"				{\n" + 
			"					rs.toString();\n" + 
			"				}\n" + 
			"				catch (Exception ex)\n" + 
			"				{\n" + 
			"				}\n" + 
			"			}\n" + 
			"		}\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"\n" + 
			"	private void dgagadga()\n" + 
			"	{\n" + 
			"	}\n" + 
			"	\n" + 
			"	private String adsyasta;\n" + 
			"}\n",
		},
		"");
}
/*
 * Check scenario:  i = i++
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=84480
 */
public void test035() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	int f;\n" + 
			"	void foo(int i) {\n" + 
			"		i = i++;\n" + 
			"		i = ++i;\n" + 
			"		f = f++;\n" + 
			"		f = ++f;\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	i = i++;\n" + 
		"	^^^^^^^\n" + 
		"The assignment to variable i has no effect\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	f = f++;\n" + 
		"	^^^^^^^\n" + 
		"The assignment to variable f has no effect\n" + 
		"----------\n");
}
public void test036() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"\n" + 
			"	void foo() {\n" + 
			"		Object o = new Object();\n" + 
			"		do {\n" + 
			"			o = null;\n" + 
			"		} while (o != null);\n" + 
			"		if (o == null) {\n" + 
			"			// throw new Exception();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\r\n" + 
		"	if (o == null) {\r\n" + 
		"	    ^\n" + 
		"The variable o cannot be null; it was either set to a non-null value or assumed to be non-null when last used\n" + 
		"----------\n");
}
public static Class testClass() {
	return AssignmentTest.class;
}
}
