package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AssignmentTest extends AbstractRegressionTest {
	
public AssignmentTest(String name) {
	super(name);
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
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
/*
 * check null/non-null reference diagnosis
 */
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void _test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
			"			}\n" + 
			"		}\n" + 
			"		if (c == null) {\n" + 
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
		"1. ERROR in X.java (at line 21)\n" + 
		"	if (c == null) {\n" + 
		"	    ^\n" + 
		"The variable c can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test015() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test016() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test017() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"1. ERROR in X.java (at line 8)\n" + 
		"	x.foo(null);\n" + 
		"	^\n" + 
		"The variable x can only be null; it was either set to null or checked for null when last used\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportInconsistentNullCheck, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}

public static Class testClass() {
	return AssignmentTest.class;
}
}
