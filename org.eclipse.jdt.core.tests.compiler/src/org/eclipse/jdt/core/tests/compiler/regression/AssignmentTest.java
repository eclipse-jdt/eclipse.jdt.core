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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
// TODO (philippe) reenable once fixed
public void _test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
public void test019() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
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
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test022() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test024() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test025() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
// TODO (philippe) reenable once fixed
public void _test026() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null);
}
// TODO (philippe) reenable once fixed
public void _test027() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
// TODO (philippe) reenable once fixed
public void _test028() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test029() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"SUCCESS",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test030() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
// TODO (philippe) reenable once fixed
public void _test031() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}
public void test032() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
public void test033() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.ERROR);
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
		"----------\n",
		null,
		true,
		customOptions);
}

public static Class testClass() {
	return AssignmentTest.class;
}
}
