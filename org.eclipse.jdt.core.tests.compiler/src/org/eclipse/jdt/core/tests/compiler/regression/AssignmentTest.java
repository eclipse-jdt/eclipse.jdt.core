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

public static Class testClass() {
	return AssignmentTest.class;
}
}
