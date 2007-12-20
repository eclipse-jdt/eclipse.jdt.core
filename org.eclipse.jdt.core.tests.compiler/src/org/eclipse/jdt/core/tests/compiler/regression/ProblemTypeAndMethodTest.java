/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

public class ProblemTypeAndMethodTest extends AbstractRegressionTest {
public ProblemTypeAndMethodTest(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 169, 180 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {  
	return ProblemTypeAndMethodTest.class;
}

public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"        interface Moosh { void foo(); }\n" + 
			"\n" + 
			"        static abstract class A implements Moosh {}\n" + 
			"\n" + 
			"        static class W extends A {}\n" + 
			"        static class Y extends A {}\n" + 
			"        static class Z extends A {}\n" + 
			"        public static void main(String[] args) {\n" + 
			"                new W();  // throws ClassFormatError\n" + 
			"        }\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	static class W extends A {}\n" + 
		"	             ^\n" + 
		"The type X.W must implement the inherited abstract method X.Moosh.foo()\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	static class Y extends A {}\n" + 
		"	             ^\n" + 
		"The type X.Y must implement the inherited abstract method X.Moosh.foo()\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	static class Z extends A {}\n" + 
		"	             ^\n" + 
		"The type X.Z must implement the inherited abstract method X.Moosh.foo()\n" + 
		"----------\n",
		null /* no extra class libraries */, 
		true /* flush output directory */, 
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */, 
		false /* do not show warning token */, 
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	ClassFileReader reader = this.getClassFileReader(OUTPUT_DIR + File.separator  +"X$W.class", "X$W");
	IBinaryMethod[] methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	int counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
	
	reader = this.getClassFileReader(OUTPUT_DIR + File.separator  +"X$Y.class", "X$Y");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
	
	reader = this.getClassFileReader(OUTPUT_DIR + File.separator  +"X$Z.class", "X$Z");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test016() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" + 
			"	java.langz.AClass1 field1;\n" + 
			"	java.langz.AClass2 field2;\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	java.langz.AClass1 field1;\n" + 
		"	^^^^^^^^^^\n" + 
		"java.langz cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	java.langz.AClass2 field2;\n" + 
		"	^^^^^^^^^^\n" + 
		"java.langz cannot be resolved to a type\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test017() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" + 
			"	java.langz field1;\n" + 
			"	java.langz.AClass2 field2;\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	java.langz field1;\n" + 
		"	^^^^^^^^^^\n" + 
		"java.langz cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	java.langz.AClass2 field2;\n" + 
		"	^^^^^^^^^^\n" + 
		"java.langz cannot be resolved to a type\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test018() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" + 
			"	java.langz.AClass1 field1;\n" + 
			"	java.langz field2;\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	java.langz.AClass1 field1;\n" + 
		"	^^^^^^^^^^\n" + 
		"java.langz cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	java.langz field2;\n" + 
		"	^^^^^^^^^^\n" + 
		"java.langz cannot be resolved to a type\n" + 
		"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test019() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q1.q2.Zork;\n" + 
					"\n" + 
					"public class OtherFoo extends Zork{\n" + 
					"	public class OtherMember extends Zork {}\n" + 
					"	public Zork foo;\n" + 
					"	public Zork bar() {	return null; }\n" + 
					"	public void baz(Zork z) {}\n" + 
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	
	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(p.OtherFoo ofoo) {\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		a.b.Missing1 m1;\n" +
					"		q1.q2.Missing2 m2;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	q1.q2.Missing2 m2;\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"q1.q2.Missing2 cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test020() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q1.q2.Zork;\n" + 
					"\n" + 
					"public class OtherFoo extends Zork{\n" + 
					"	public class OtherMember extends Zork {}\n" + 
					"	public Zork foo;\n" + 
					"	public Zork bar() {	return null; }\n" + 
					"	public void baz(Zork z) {}\n" + 
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	
	// no need to delete Zork actually - any lazy reference would cause q1.q2 to be created as a package
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(p.OtherFoo ofoo) {\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		a.b.Missing1 m1;\n" +
					"		q1.q2.Missing2 m2;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	q1.q2.Missing2 m2;\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"q1.q2.Missing2 cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test021() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(p.OtherFoo ofoo) {\n" + 
					"		a.b.Missing1 m1;\n" +
					"		q1.q2.Missing2 m2;\n" +
					"	}\n" + 
					"}	\n",
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"public class OtherFoo extends q1.q2.Zork{\n" + 
					"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	q1.q2.Missing2 m2;\n" + 
			"	^^\n" + 
			"q1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in p\\OtherFoo.java (at line 2)\n" + 
			"	public class OtherFoo extends q1.q2.Zork{\n" + 
			"	                              ^^\n" + 
			"q1 cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test022() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q1.q2.Zork;\n" + 
					"\n" + 
					"public class OtherFoo {\n" + 
					"	public Zork foo;\n" + 
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	
	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(q1.q2.Missing1 m1) {\n" + 
			"	         ^^\n" + 
			"q1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1a;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	q1.q2.Missing1 m11;\n" + 
			"	^^\n" + 
			"q1 cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test023() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q1.q2.Zork;\n" + 
					"\n" + 
					"public class OtherFoo {\n" + 
					"	public Zork foo;\n" + 
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	
	// leave package behind
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" +
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(q1.q2.Missing1 m1) {\n" + 
			"	         ^^^^^^^^^^^^^^\n" + 
			"q1.q2.Missing1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1a;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	q1.q2.Missing1 m11;\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"q1.q2.Missing1 cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test024() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q1.q2.Zork;\n" + 
					"\n" + 
					"public class OtherFoo {\n" + 
					"	public Zork foo;\n" + 
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	
	// delete binary folder q1/q2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1/q2"));
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(q1.q2.Missing1 m1) {\n" + 
			"	         ^^^^^\n" + 
			"q1.q2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1a;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	q1.q2.Missing1 m11;\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"q1.q2.Missing1 cannot be resolved to a type\n" + // inconsistent msg from previous one (error 1)
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test025() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q1.q2.Zork;\n" + 
					"\n" + 
					"public class OtherFoo {\n" + 
					"	public Zork foo;\n" + 
					"}\n",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"package q1.q2;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	
	// delete binary folder q1/q2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1/q2"));
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(q1.q2.Missing1 m1) {\n" +
					"		a.b.Missing1 m1a;\n" +
					"		p.OtherFoo ofoo;\n" + // triggers OtherFoo loading, and q1.q2 pkg creation (for unresolved binary type refs)
					"	}\n" + 
					"}	\n",
					"Y.java", //-----------------------------------------------------------------------
					"public class Y {\n" + 
					"	void foo() {\n" +
					"		q1.q2.Missing1 m11;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(q1.q2.Missing1 m1) {\n" + 
			"	         ^^^^^\n" + 
			"q1.q2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	a.b.Missing1 m1a;\n" + 
			"	^\n" + 
			"a cannot be resolved to a type\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 3)\n" + 
			"	q1.q2.Missing1 m11;\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"q1.q2.Missing1 cannot be resolved to a type\n" +  // inconsistent msg from previous one (error 1)
			"----------\n",
			null,
			false);
}

}
