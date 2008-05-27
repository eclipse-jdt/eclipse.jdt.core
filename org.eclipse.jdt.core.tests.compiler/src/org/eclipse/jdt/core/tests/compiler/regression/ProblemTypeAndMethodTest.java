/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
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

public void test002() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X extends Zork {\n" + 
			"	void foo() {\n" + 
			"		Zork z = this;\n" + 
			"		String s = this;\n" + 
			"		Zork2 z2 = this;\n" + 
			"	}\n" + 
			"	Zork fz = this;\n" + 
			"	String fs = this;\n" + 
			"	Zork2 fz2 = this;\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X extends Zork {\n" + 
		"	                       ^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	Zork z = this;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	String s = this;\n" + 
		"	           ^^^^\n" + 
		"Type mismatch: cannot convert from X to String\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 5)\n" + 
		"	Zork2 z2 = this;\n" + 
		"	^^^^^\n" + 
		"Zork2 cannot be resolved to a type\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 7)\n" + 
		"	Zork fz = this;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 8)\n" + 
		"	String fs = this;\n" + 
		"	            ^^^^\n" + 
		"Type mismatch: cannot convert from X to String\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 9)\n" + 
		"	Zork2 fz2 = this;\n" + 
		"	^^^^^\n" + 
		"Zork2 cannot be resolved to a type\n" + 
		"----------\n");
}

public void test003() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class X {\n" + 
			"	Zork field;\n" + 
			"	\n" + 
			"	void foo(Y y) {\n" + 
			"		Object o = y.foo();\n" + 
			"		Object s = y.slot;\n" + 
			"		y.bar(null);\n" + 
			"		Object s2 = new Y().slot;\n" + 
			"		Object f = field;\n" + 
			"	}\n" + 
			"}\n" + 
			"class Y {\n" + 
			"	Zork foo() {	return null; }\n" + 
			"	void bar(Zork z) {}\n" + 
			"	Zork slot;\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	Zork field;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 5)\n" + 
		"	Object o = y.foo();\n" + 
		"	             ^^^\n" + 
		"The method foo() from the type Y refers to the missing type Zork\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 6)\n" + 
		"	Object s = y.slot;\n" + 
		"	           ^^^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 7)\n" + 
		"	y.bar(null);\n" + 
		"	  ^^^\n" + 
		"The method bar(Zork) from the type Y refers to the missing type Zork\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 8)\n" + 
		"	Object s2 = new Y().slot;\n" + 
		"	            ^^^^^^^^^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 9)\n" + 
		"	Object f = field;\n" + 
		"	           ^^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 13)\n" + 
		"	Zork foo() {	return null; }\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 14)\n" + 
		"	void bar(Zork z) {}\n" + 
		"	         ^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"9. ERROR in X.java (at line 15)\n" + 
		"	Zork slot;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200
public void test004() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"package p;\n" + 
					"\n" + 
					"import q.Zork;\n" + 
					"\n" + 
					"public class OtherFoo extends Zork{\n" + 
					"	public class OtherMember extends Zork {}\n" + 
					"	public Zork foo;\n" + 
					"	public Zork bar() {	return null; }\n" + 
					"	public void baz(Zork z) {}\n" + 
					"}\n",
					"q/Zork.java", //-----------------------------------------------------------------------
					"package q;\n" + 
					"public class Zork {\n" +
					"}\n",
			},
			"");	
	this.runNegativeTest(
		new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import p.OtherFoo;\n" + 
				"import q.Zork;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		OtherFoo ofoo;\n" + 
				"		String s1 = ofoo.foo;\n" + 
				"		String s2 = ofoo.bar();\n" + 
				"		String s3 = ofoo.new OtherMember();\n" + 
				"		ofoo.baz(this);\n" + 
				"	}\n" + 
				"	void bar() {\n" + 
				"		OtherX ox;\n" + 
				"		String s1 = ox.foo;\n" + 
				"		String s2 = ox.bar();\n" + 
				"		String s3 = ox.new OtherMember();\n" + 
				"		ox.baz(this);\n" + 
				"	}\n" + 
				"}	\n" + 
				"\n" + 
				"class OtherX {\n" + 
				"	public class OtherMember extends Zork {}\n" + 
				"	public Zork foo;\n" + 
				"	public Zork bar() {	return null; }\n" + 
				"	public void baz(Zork z) {}\n" + 
				"}\n",				
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	String s1 = ofoo.foo;\n" + 
		"	            ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Zork to String\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	String s2 = ofoo.bar();\n" + 
		"	            ^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Zork to String\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 9)\n" + 
		"	String s3 = ofoo.new OtherMember();\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from OtherFoo.OtherMember to String\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 10)\n" + 
		"	ofoo.baz(this);\n" + 
		"	     ^^^\n" + 
		"The method baz(Zork) in the type OtherFoo is not applicable for the arguments (X)\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 14)\n" + 
		"	String s1 = ox.foo;\n" + 
		"	            ^^^^^^\n" + 
		"Type mismatch: cannot convert from Zork to String\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 15)\n" + 
		"	String s2 = ox.bar();\n" + 
		"	            ^^^^^^^^\n" + 
		"Type mismatch: cannot convert from Zork to String\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 16)\n" + 
		"	String s3 = ox.new OtherMember();\n" + 
		"	            ^^^^^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from OtherX.OtherMember to String\n" + 
		"----------\n" + 
		"8. ERROR in X.java (at line 17)\n" + 
		"	ox.baz(this);\n" + 
		"	   ^^^\n" + 
		"The method baz(Zork) in the type OtherX is not applicable for the arguments (X)\n" + 
		"----------\n",
		null,
		false);
	
	// delete binary file Zork (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q" + File.separator + "Zork.class"));
	
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"import p.OtherFoo;\n" + 
					"import q.Zork;\n" + 
					"\n" + 
					"public class X {\n" + 
					"	void foo() {\n" + 
					"		OtherFoo ofoo;\n" + 
					"		String s1 = ofoo.foo;\n" + 
					"		String s2 = ofoo.bar();\n" + 
					"		String s3 = ofoo.new OtherMember();\n" + 
					"		ofoo.baz(this);\n" + 
					"	}\n" + 
					"	void bar() {\n" + 
					"		OtherX ox;\n" + 
					"		String s1 = ox.foo;\n" + 
					"		String s2 = ox.bar();\n" + 
					"		String s3 = ox.new OtherMember();\n" + 
					"		ox.baz(this);\n" + 
					"	}\n" + 
					"}	\n" + 
					"\n" + 
					"class OtherX {\n" + 
					"	public class OtherMember extends Zork {}\n" + 
					"	public Zork foo;\n" + 
					"	public Zork bar() {	return null; }\n" + 
					"	public void baz(Zork z) {}\n" + 
					"}\n",				
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import p.OtherFoo;\n" + 
			"	^\n" + 
			"The type q.Zork cannot be resolved. It is indirectly referenced from required .class files\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	import q.Zork;\n" + 
			"	       ^^^^^^\n" + 
			"The import q.Zork cannot be resolved\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	String s1 = ofoo.foo;\n" + 
			"	            ^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 8)\n" + 
			"	String s2 = ofoo.bar();\n" + 
			"	                 ^^^\n" + 
			"The method bar() from the type OtherFoo refers to the missing type Zork\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 9)\n" + 
			"	String s3 = ofoo.new OtherMember();\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from OtherFoo.OtherMember to String\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 10)\n" + 
			"	ofoo.baz(this);\n" + 
			"	     ^^^\n" + 
			"The method baz(Zork) from the type OtherFoo refers to the missing type Zork\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 14)\n" + 
			"	String s1 = ox.foo;\n" + 
			"	            ^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 15)\n" + 
			"	String s2 = ox.bar();\n" + 
			"	               ^^^\n" + 
			"The method bar() from the type OtherX refers to the missing type Zork\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 16)\n" + 
			"	String s3 = ox.new OtherMember();\n" + 
			"	            ^^^^^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from OtherX.OtherMember to String\n" + 
			"----------\n" + 
			"10. ERROR in X.java (at line 17)\n" + 
			"	ox.baz(this);\n" + 
			"	   ^^^\n" + 
			"The method baz(Zork) from the type OtherX refers to the missing type Zork\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 22)\n" + 
			"	public class OtherMember extends Zork {}\n" + 
			"	                                 ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"12. ERROR in X.java (at line 23)\n" + 
			"	public Zork foo;\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"13. ERROR in X.java (at line 24)\n" + 
			"	public Zork bar() {	return null; }\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"14. ERROR in X.java (at line 25)\n" + 
			"	public void baz(Zork z) {}\n" + 
			"	                ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test005() {
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
					"	void foo() {\n" + 
					"		p.OtherFoo ofoo = new p.OtherFoo();\n" +
					"		ofoo.bar();\n" +
					"		q1.q2.Zork z;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	ofoo.bar();\n" + 
			"	^^^^^^^^^^\n" + 
			"The type q1.q2.Zork cannot be resolved. It is indirectly referenced from required .class files\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	ofoo.bar();\n" + 
			"	     ^^^\n" + 
			"The method bar() from the type OtherFoo refers to the missing type Zork\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	q1.q2.Zork z;\n" + 
			"	^^^^^^^^^^\n" + 
			"q1.q2.Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test006() {
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
					"import q1.q2.*;\n" +
					"public class X {\n" + 
					"	void foo() {\n" + 
					"		p.OtherFoo ofoo = new p.OtherFoo();\n" +
					"		ofoo.bar();\n" +
					"		Zork z;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import q1.q2.*;\n" + 
			"	       ^^\n" + 
			"The import q1 cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	ofoo.bar();\n" + 
			"	^^^^^^^^^^\n" + 
			"The type q1.q2.Zork cannot be resolved. It is indirectly referenced from required .class files\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	ofoo.bar();\n" + 
			"	     ^^^\n" + 
			"The method bar() from the type OtherFoo refers to the missing type Zork\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test007() {
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
					"import q1.q2.Zork;\n" +
					"public class X {\n" + 
					"	void foo() {\n" + 
					"		p.OtherFoo ofoo = new p.OtherFoo();\n" +
					"		ofoo.bar();\n" +
					"		Zork z;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import q1.q2.Zork;\n" + 
			"	       ^^\n" + 
			"The import q1 cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	ofoo.bar();\n" + 
			"	^^^^^^^^^^\n" + 
			"The type q1.q2.Zork cannot be resolved. It is indirectly referenced from required .class files\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	ofoo.bar();\n" + 
			"	     ^^^\n" + 
			"The method bar() from the type OtherFoo refers to the missing type Zork\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test008() {
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
					"	void foo() {\n" + 
					"		q1.q2.Zork z;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	q1.q2.Zork z;\n" + 
			"	^^\n" + 
			"q1 cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test009() {
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
					"import q1.q2.*;\n" +
					"public class X {\n" + 
					"	void foo() {\n" + 
					"		Zork z;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import q1.q2.*;\n" + 
			"	       ^^\n" + 
			"The import q1 cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test010() {
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
					"import q1.q2.Zork;\n" +
					"public class X {\n" + 
					"	void foo() {\n" + 
					"		Zork z;\n" +
					"	}\n" + 
					"}	\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	import q1.q2.Zork;\n" + 
			"	       ^^\n" + 
			"The import q1 cannot be resolved\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test011() {
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" + 
			"	java[] field1;\n" + 
			"	java.lang[] field2;\n" + 
			"	void field3;\n" + 
			"	void[] field4;\n" + 
			"	\n" + 
			"}\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	java[] field1;\n" + 
		"	^^^^\n" + 
		"java cannot be resolved to a type\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 3)\n" + 
		"	java.lang[] field2;\n" + 
		"	^^^^^^^^^\n" + 
		"java.lang cannot be resolved to a type\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 4)\n" + 
		"	void field3;\n" + 
		"	     ^^^^^^\n" + 
		"void is an invalid type for the variable field3\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 5)\n" + 
		"	void[] field4;\n" + 
		"	^^^^^^\n" + 
		"void[] is an invalid type\n" + 
		"----------\n");
}
public void test012() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult = 
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Class c1 = java[].class;\n" + 
			"	           ^^^^\n" + 
			"java cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	           ^^^^^^^^^\n" + 
			"java.lang cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Class c4 = void[].class;\n" + 
			"	           ^^^^^^\n" + 
			"void[] is an invalid type\n" + 
			"----------\n";
	} else {
		expectedResult = 
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	Class c1 = java[].class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	Class c1 = java[].class;\n" + 
			"	           ^^^^\n" + 
			"java cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	           ^^^^^^^^^\n" + 
			"java.lang cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 4)\n" + 
			"	Class c3 = void.class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 5)\n" + 
			"	Class c4 = void[].class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 5)\n" + 
			"	Class c4 = void[].class;\n" + 
			"	           ^^^^^^\n" + 
			"void[] is an invalid type\n" + 
			"----------\n";		
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" + 
			"	Class c1 = java[].class;\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	Class c3 = void.class;\n" + 
			"	Class c4 = void[].class;\n" + 
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test013() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult = 
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	           ^^^^^^^^^\n" + 
			"java.lang cannot be resolved to a type\n" + 
			"----------\n";
	} else {
		expectedResult =
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	           ^^^^^^^^^\n" + 
			"java.lang cannot be resolved to a type\n" + 
			"----------\n";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" + 
			"	// check if no prior reference to missing 'java'\n" +
			"	Class c2 = java.lang[].class;\n" + 
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test014() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult = 	
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	           ^^^^^^^^^\n" + 
			"java.lang cannot be resolved to a type\n" + 
			"----------\n";
	} else {
		expectedResult =
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Class c2 = java.lang[].class;\n" + 
			"	           ^^^^^^^^^\n" + 
			"java.lang cannot be resolved to a type\n" + 
			"----------\n";			
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" + 
			"	// check if no prior reference to missing 'java'\n" +
			"	Class c2 = java.lang[].class;\n" + 
			"}\n",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test015() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult = 		
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	Class a = zork1[].class;\n" + 
			"	          ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Class x = zork1.zork2[].class;	// compile time error\n" + 
			"	          ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Class a2 = zork1.class;\n" + 
			"	           ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" + 
			"	           ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n";
	} else {
		expectedResult =
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	Class a = zork1[].class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	Class a = zork1[].class;\n" + 
			"	          ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 3)\n" + 
			"	Class x = zork1.zork2[].class;	// compile time error\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 3)\n" + 
			"	Class x = zork1.zork2[].class;	// compile time error\n" + 
			"	          ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. WARNING in X.java (at line 5)\n" + 
			"	Class a2 = zork1.class;\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 5)\n" + 
			"	Class a2 = zork1.class;\n" + 
			"	           ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. WARNING in X.java (at line 6)\n" + 
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" + 
			"	^^^^^\n" + 
			"Class is a raw type. References to generic type Class<T> should be parameterized\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 6)\n" + 
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" + 
			"	           ^^^^^\n" + 
			"zork1 cannot be resolved to a type\n" + 
			"----------\n";			
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"public class  X {\n" + 
			"	Class a = zork1[].class;\n" + 
			"	Class x = zork1.zork2[].class;	// compile time error\n" + 
			"	\n" + 
			"	Class a2 = zork1.class;\n" + 
			"	Class x2 = zork1.zork2.class;	// compile time error	\n" + 
			"}\n",
		},
		expectedResult);
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test026() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(Missing1 m1) {\n" + 
					"		Missing2 m2 = m1;\n" + 
					"	}\n" + 
					"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(Missing1 m1) {\n" + 
			"	         ^^^^^^^^\n" + 
			"Missing1 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	Missing2 m2 = m1;\n" + 
			"	^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test027() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(X x) {\n" + 
					"		new Other().m2 = x;\n" + 
					"		Other other = new Other();\n" + 
					"		other.m2 = x;\n" + 
					"		other.m2.m3 = x;\n" + 
					"	}\n" + 
					"}\n" + 
					"\n" + 
					"class Other {\n" + 
					"	Missing2 m2;\n" + 
					"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	new Other().m2 = x;\n" + 
			"	^^^^^^^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	other.m2 = x;\n" + 
			"	^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	other.m2.m3 = x;\n" + 
			"	^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 11)\n" + 
			"	Missing2 m2;\n" + 
			"	^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test028() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"public class X {\n" + 
					"	void foo(X x) {\n" + 
					"		System.out.println(new Other().m2.m3);\n" + 
					"		System.out.println(new Other().m2.m3());\n" + 
					"		Missing2.foo();\n" + 
					"	}\n" + 
					"}\n" + 
					"\n" + 
					"class Other {\n" + 
					"	Missing2 m2;\n" + 
					"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	System.out.println(new Other().m2.m3);\n" + 
			"	                   ^^^^^^^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	System.out.println(new Other().m2.m3());\n" + 
			"	                   ^^^^^^^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	Missing2.foo();\n" + 
			"	^^^^^^^^\n" + 
			"Missing2 cannot be resolved\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	Missing2 m2;\n" + 
			"	^^^^^^^^\n" + 
			"Missing2 cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test029() throws Exception {
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	public class Y extends Z {\n" + 
			"	                       ^\n" + 
			"Z cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	// check Y superclass in problem classfile
	String expectedOutput =
		"public class Y extends Z {\n";
	
	File f = new File(OUTPUT_DIR + File.separator + "Y.class");
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test030() {
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	public class Y extends Z {\n" + 
			"	                       ^\n" + 
			"Z cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X extends Y {\n" + 
			"	             ^\n" + 
			"The hierarchy of the type X is inconsistent\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test031() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" + 
				"}\n",					
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X extends Y {\n" + 
			"	             ^\n" + 
			"The hierarchy of the type X is inconsistent\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	public class Y extends Z {\n" + 
			"	                       ^\n" + 
			"Z cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test032() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import existing.*;\n" + 
				"public class X {\n" + 
				"  void foo(p.Zork z) {}\n" + 
				"  void bar(Zork z) {} // should bind to existing.Zork\n" + 
				"}\n",
				"p/Clyde.java", //-----------------------------------------------------------------------
				"package p;\n" + // just so package p does exist
				"public class Clyde {\n" + 
				"}\n",
				"existing/Zork.java", //-----------------------------------------------------------------------
				"package existing;\n" + 
				"public class Zork {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	void foo(p.Zork z) {}\n" + 
			"	         ^^^^^^\n" + 
			"p.Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test033() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		return;
	}
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"@Z public class Y {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Y.java (at line 1)\n" + 
			"	@Z public class Y {\n" + 
			"	 ^\n" + 
			"Z cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runConformTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" + 
				"}\n",
			},
			"",
			null,
			false, // do not flush output
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test034() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = x1.bar();\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	Zork bar() { return null; }	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = x1.bar();\n" + 
			"	              ^^^\n" + 
			"The method bar() from the type X1 refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	Zork bar() { return null; }	\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test035() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	Zork bar() { return null; }	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	Zork bar() { return null; }	\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = x1.bar();\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = x1.bar();\n" + 
			"	              ^^^\n" + 
			"The method bar() from the type X1 refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test036() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = x1.bar(x1);\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	Object bar(Zork z) { return null; }	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = x1.bar(x1);\n" + 
			"	              ^^^\n" + 
			"The method bar(Zork) from the type X1 refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	Object bar(Zork z) { return null; }	\n" + 
			"	           ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test037() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	Object bar(Zork z) { return null; }	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	Object bar(Zork z) { return null; }	\n" + 
			"	           ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = x1.bar(x1);\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = x1.bar(x1);\n" + 
			"	              ^^^\n" + 
			"The method bar(Zork) from the type X1 refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test038() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = x1.bar(x1);\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	Object bar(Object o) throws Zork { return null; }	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = x1.bar(x1);\n" + 
			"	              ^^^\n" + 
			"The method bar(Object) from the type X1 refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	Object bar(Object o) throws Zork { return null; }	\n" + 
			"	                            ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test039() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	Object bar(Object o) throws Zork { return null; }	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	Object bar(Object o) throws Zork { return null; }	\n" + 
			"	                            ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = x1.bar(x1);\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = x1.bar(x1);\n" + 
			"	              ^^^\n" + 
			"The method bar(Object) from the type X1 refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test040() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1(x1);\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1(x1);\n" + 
			"	           ^^^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1(Zork z) {}\n" + 
			"	          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test041() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1(Zork z) {}\n" + 
			"	          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1(x1);\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1(x1);\n" + 
			"	           ^^^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test042() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1();\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1() throws Zork {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1();\n" + 
			"	           ^^^^^^^^\n" + 
			"The constructor X1() refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1() throws Zork {}\n" + 
			"	                   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test043() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1() throws Zork {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1() throws Zork {}\n" + 
			"	                   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1();\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1();\n" + 
			"	           ^^^^^^^^\n" + 
			"The constructor X1() refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test044() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1(x1){};\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1(x1){};\n" + 
			"	               ^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1(Zork z) {}\n" + 
			"	          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test045() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1(Zork z) {}\n" + 
			"	          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1(x1){};\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1(x1){};\n" + 
			"	               ^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test046() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1(){};\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1() throws Zork {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1(){};\n" + 
			"	               ^^^^\n" + 
			"The constructor X1() refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1() throws Zork {}\n" + 
			"	                   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test047() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1() throws Zork {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1() throws Zork {}\n" + 
			"	                   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o = new X1(){};\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Object o = new X1(){};\n" + 
			"	               ^^^^\n" + 
			"The constructor X1() refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test048() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" + 
				"	X(X1 x1) {\n" + 
				"		super(x1);\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	super(x1);\n" + 
			"	^^^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1(Zork z) {}\n" + 
			"	          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test049() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1(Zork z) {}\n" + 
			"	          ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" + 
				"	X(X1 x1) {\n" + 
				"		super(x1);\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	super(x1);\n" + 
			"	^^^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test050() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" + 
				"	X(X1 x1) {\n" + 
				"		super();\n" + 
				"	}\n" + 
				"}\n",
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1() throws Zork {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	super();\n" + 
			"	^^^^^^^^\n" + 
			"The constructor X1() refers to the missing type Zork\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1() throws Zork {}\n" + 
			"	                   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test051() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1() throws Zork {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 2)\n" + 
			"	public X1() throws Zork {}\n" + 
			"	                   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends X1 {\n" + 
				"	X(X1 x1) {\n" + 
				"		super();\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	super();\n" + 
			"	^^^^^^^^\n" + 
			"The constructor X1() refers to the missing type Zork\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test052() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o;\n" + 
				"		o = x1.next.zork;\n" + 
				"		o = this.zork;\n" + 
				"		o = zork;\n" + 
				"		o = x1.next.zork.foo();\n" + 
				"		o = this.zork.foo();\n" + 
				"		o = zork.foo();\n" + 
				"	}\n" + 
				"	Zork zork;\n" + 
				"}\n" + 
				"class X1 {\n" + 
				"	X1 next;\n" + 
				"	Zork zork;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	o = x1.next.zork;\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	o = this.zork;\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	o = zork;\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 7)\n" + 
			"	o = x1.next.zork.foo();\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 8)\n" + 
			"	o = this.zork.foo();\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 9)\n" + 
			"	o = zork.foo();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 11)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 15)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test053() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void bar(X1 x1) {\n" + 
				"		String s;\n" + 
				"		s = x1.next.zork;\n" + 
				"		s = this.zork;\n" + 
				"		s = zork;\n" + 
				"		s = x1.next.zork.foo();\n" + 
				"		s = this.zork.foo();\n" + 
				"		s = zork.foo();\n" + 
				"	}	\n" + 
				"	Zork zork;\n" + 
				"}\n" + 
				"class X1 {\n" + 
				"	X1 next;\n" + 
				"	Zork zork;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	s = x1.next.zork;\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	s = this.zork;\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	s = zork;\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 7)\n" + 
			"	s = x1.next.zork.foo();\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 8)\n" + 
			"	s = this.zork.foo();\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 9)\n" + 
			"	s = zork.foo();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 11)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 15)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test054() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void baz(X1 x1) {\n" + 
				"		Zork z;\n" + 
				"		z = x1.next.zork;\n" + 
				"		z = this.zork;\n" + 
				"		z = zork;\n" + 
				"		z = x1.next.zork.foo();\n" + 
				"		z = this.zork.foo();\n" + 
				"		z = zork.foo();\n" + 
				"	}	\n" + 
				"	Zork zork;\n" + 
				"}\n" + 
				"class X1 {\n" + 
				"	X1 next;\n" + 
				"	Zork zork;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	z = x1.next.zork;\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	z = this.zork;\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	z = zork;\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 7)\n" + 
			"	z = x1.next.zork.foo();\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 8)\n" + 
			"	z = this.zork.foo();\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 9)\n" + 
			"	z = zork.foo();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 11)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 15)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test055() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1 next;\n" + 
				"	public Zork zork;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 3)\n" + 
			"	public Zork zork;\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		Object o;\n" + 
				"		o = x1.next.zork;\n" + 
				"		o = this.zork;\n" + 
				"		o = zork;\n" + 
				"		o = x1.next.zork.foo();\n" + 
				"		o = this.zork.foo();\n" + 
				"		o = zork.foo();\n" + 
				"	}\n" + 
				"	Zork zork;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	o = x1.next.zork;\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	o = this.zork;\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	o = zork;\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 7)\n" + 
			"	o = x1.next.zork.foo();\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 8)\n" + 
			"	o = this.zork.foo();\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 9)\n" + 
			"	o = zork.foo();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 11)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test056() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1 next;\n" + 
				"	public Zork zork;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 3)\n" + 
			"	public Zork zork;\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void bar(X1 x1) {\n" + 
				"		String s;\n" + 
				"		s = x1.next.zork;\n" + 
				"		s = this.zork;\n" + 
				"		s = zork;\n" + 
				"		s = x1.next.zork.foo();\n" + 
				"		s = this.zork.foo();\n" + 
				"		s = zork.foo();\n" + 
				"	}	\n" + 
				"	Zork zork;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	s = x1.next.zork;\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	s = this.zork;\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 6)\n" + 
			"	s = zork;\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 7)\n" + 
			"	s = x1.next.zork.foo();\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 8)\n" + 
			"	s = this.zork.foo();\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 9)\n" + 
			"	s = zork.foo();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 11)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test057() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"public class X1 {\n" + 
				"	public X1 next;\n" + 
				"	public Zork zork;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X1.java (at line 3)\n" + 
			"	public Zork zork;\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void baz(X1 x1) {\n" + 
				"		Zork z;\n" + 
				"		z = x1.next.zork;\n" + 
				"		z = this.zork;\n" + 
				"		z = zork;\n" + 
				"		z = x1.next.zork.foo();\n" + 
				"		z = this.zork.foo();\n" + 
				"		z = zork.foo();\n" + 
				"	}	\n" + 
				"	Zork zork;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	z = x1.next.zork;\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	z = this.zork;\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 6)\n" + 
			"	z = zork;\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 7)\n" + 
			"	z = x1.next.zork.foo();\n" + 
			"	    ^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 8)\n" + 
			"	z = this.zork.foo();\n" + 
			"	    ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 9)\n" + 
			"	z = zork.foo();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 11)\n" + 
			"	Zork zork;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test058() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		x1.bar().baz();\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class X1 {\n" + 
				"	Zork bar(){}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	x1.bar().baz();\n" + 
			"	   ^^^\n" + 
			"The method bar() from the type X1 refers to the missing type Zork\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	Zork bar(){}\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test059() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		new X1(x1).baz();\n" + 
				"		new X1(null).baz();\n" + 
				"		new Zork().baz();\n" + 
				"		new X1(x1){}.baz();\n" + 
				"		new X1(null){}.baz();\n" + 
				"		new Zork(){}.baz();\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class X1 {\n" + 
				"	X1(Zork z) {}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	new X1(x1).baz();\n" + 
			"	^^^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	new X1(x1).baz();\n" + 
			"	           ^^^\n" + 
			"The method baz() is undefined for the type X1\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	new X1(null).baz();\n" + 
			"	^^^^^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 4)\n" + 
			"	new X1(null).baz();\n" + 
			"	             ^^^\n" + 
			"The method baz() is undefined for the type X1\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 5)\n" + 
			"	new Zork().baz();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 6)\n" + 
			"	new X1(x1){}.baz();\n" + 
			"	    ^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 6)\n" + 
			"	new X1(x1){}.baz();\n" + 
			"	             ^^^\n" + 
			"The method baz() is undefined for the type new X1(){}\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 7)\n" + 
			"	new X1(null){}.baz();\n" + 
			"	    ^^^^^^^^\n" + 
			"The constructor X1(Zork) refers to the missing type Zork\n" + 
			"----------\n" + 
			"9. ERROR in X.java (at line 7)\n" + 
			"	new X1(null){}.baz();\n" + 
			"	               ^^^\n" + 
			"The method baz() is undefined for the type new X1(){}\n" + 
			"----------\n" + 
			"10. ERROR in X.java (at line 8)\n" + 
			"	new Zork(){}.baz();\n" + 
			"	    ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"11. ERROR in X.java (at line 13)\n" + 
			"	X1(Zork z) {}\n" + 
			"	   ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test060() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(Zork z) {\n" + 
				"		z.bar();\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(Zork z) {\n" + 
			"	         ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test061() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(X1 x1) {\n" + 
				"		int i = x1.next.z;\n" + 
				"		int j = x1.next.zArray;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class X1 {\n" + 
				"	X1 next;\n" + 
				"	Zork z;\n" + 
				"	Zork[] zArray;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	int i = x1.next.z;\n" + 
			"	        ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	int j = x1.next.zArray;\n" + 
			"	        ^^^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 10)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 11)\n" + 
			"	Zork[] zArray;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test062() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	X1 x1;\n" + 
				"	void foo() {\n" + 
				"		int i = x1.next.z;\n" + 
				"		int j = x1.next.zArray;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class X1 {\n" + 
				"	X1 next;\n" + 
				"	Zork z;\n" + 
				"	Zork[] zArray;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	int i = x1.next.z;\n" + 
			"	        ^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	int j = x1.next.zArray;\n" + 
			"	        ^^^^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 11)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 12)\n" + 
			"	Zork[] zArray;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test063() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"package p;\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		int i = p.X1.z;\n" + 
				"		int j = p.X1.zArray;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class X1 {\n" + 
				"	static Zork z;\n" + 
				"	static Zork[] zArray;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 4)\n" + 
			"	int i = p.X1.z;\n" + 
			"	        ^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in p\\X.java (at line 5)\n" + 
			"	int j = p.X1.zArray;\n" + 
			"	        ^^^^^^^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in p\\X.java (at line 10)\n" + 
			"	static Zork z;\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in p\\X.java (at line 11)\n" + 
			"	static Zork[] zArray;\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test064() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.io.*;		\n" + 
				"public class X {\n" + 
				"    void foo() {\n" + 
				"        Serializable[] v= new ArrayListExtra[10];\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	Serializable[] v= new ArrayListExtra[10];\n" + 
			"	                      ^^^^^^^^^^^^^^\n" + 
			"ArrayListExtra cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test065() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.io.*;		\n" + 
				"public class X {\n" + 
				"    void foo() {\n" + 
				"    	int l = array.length;\n" + 
				"    	Object o = array[1];\n" + 
				"\n" + 
				"    }\n" + 
				"    Zork[] array;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	int l = array.length;\n" + 
			"	        ^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	Object o = array[1];\n" + 
			"	           ^^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	Zork[] array;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test066() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"        void foo() {\n" + 
				"                Zork z1 = null;\n" + 
				"                Object o = z1;\n" + 
				"                Object o1 = z1.z2;\n" + 
				"                Object o2 = bar();\n" + 
				"                Zork[] array = null;\n" + 
				"                int length = array.length;\n" + 
				"        }\n" + 
				"        Zork bar() {\n" + 
				"        }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	Zork z1 = null;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	Object o2 = bar();\n" + 
			"	            ^^^\n" + 
			"The method bar() from the type X refers to the missing type Zork\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	Zork[] array = null;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	Zork bar() {\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test067() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4)
		return;
	this.runNegativeTest(
			new String[] {
				"E.java", //-----------------------------------------------------------------------
				"public class E<T> {\n" + 
				"    class SomeType { }\n" + 
				"    void foo() {\n" + 
				"        E<XYX> list= new E<SomeType>();\n" + 
				"        list = new E<SomeType>();\n" + 
				"    }\n" + 
				"    E<XYX> fList= new E<SomeType>();\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 4)\n" + 
			"	E<XYX> list= new E<SomeType>();\n" + 
			"	  ^^^\n" + 
			"XYX cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in E.java (at line 7)\n" + 
			"	E<XYX> fList= new E<SomeType>();\n" + 
			"	  ^^^\n" + 
			"XYX cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test068() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4)
		return;
	this.runNegativeTest(
			new String[] {
				"E.java", //-----------------------------------------------------------------------
				"import java.util.Map;\n" + 
				"public class E<T> {\n" + 
				"    static class SomeType { }\n" + 
				"    void foo() {\n" + 
				"        E<Map<String, ? extends XYX>> list= new E<Map<String, ? extends SomeType>>() {\n" + 
				"        };\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 5)\n" + 
			"	E<Map<String, ? extends XYX>> list= new E<Map<String, ? extends SomeType>>() {\n" + 
			"	                        ^^^\n" + 
			"XYX cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test069() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"public class X {\n" +
				"	IOException foo() {}\n" +
				"}\n",
				"p/Y.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"import java.io.*;\n" + 
				"public class Y {\n" + 
				"   void foo(IOException e) {}\n" +
				"   void bar(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 3)\n" + 
			"	IOException foo() {}\n" + 
			"	^^^^^^^^^^^\n" + 
			"IOException cannot be resolved to a type\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in p\\Y.java (at line 5)\n" + 
			"	void bar(Zork z) {}\n" + 
			"	         ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test070() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"package p;\n" +
				"public class X {\n" + 
				"	IOException foo() {}\n" +
				"}\n",
				"q/Y.java", //-----------------------------------------------------------------------
				"package q;\n" +
				"import p.*;\n" + 
				"import java.io.*;\n" + 
				"public class Y {\n" + 
				"   void foo(IOException e) {}\n" +
				"   void bar(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in p\\X.java (at line 3)\n" + 
			"	IOException foo() {}\n" + 
			"	^^^^^^^^^^^\n" + 
			"IOException cannot be resolved to a type\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in q\\Y.java (at line 6)\n" + 
			"	void bar(Zork z) {}\n" + 
			"	         ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test071() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	IOException foo() {}\n" +
				"}\n",
				"Y.java", //-----------------------------------------------------------------------
				"import java.io.*;\n" + 
				"public class Y {\n" + 
				"   void foo(IOException e) {}\n" +
				"   void bar(Zork z) {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	IOException foo() {}\n" + 
			"	^^^^^^^^^^^\n" + 
			"IOException cannot be resolved to a type\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. ERROR in Y.java (at line 4)\n" + 
			"	void bar(Zork z) {}\n" + 
			"	         ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test072() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	public void foo() throws Foo {\n" + 
				"	}\n" + 
				"	public void bar() throws Zork {\n" + 
				"	}	\n" + 
				"}\n" + 
				"\n" + 
				"class Foo extends Zork {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public void foo() throws Foo {\n" + 
			"	                         ^^^\n" + 
			"No exception of type Foo can be thrown; an exception type must be a subclass of Throwable\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	public void bar() throws Zork {\n" + 
			"	                         ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	class Foo extends Zork {\n" + 
			"	                  ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test073() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	/**\n" + 
				"	 * @see Foo.Private#foo()\n" + 
				"	 * @param p\n" + 
				"	 */\n" + 
				"	void foo(Foo.Private p) {\n" + 
				"		p.foo();\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class Foo {\n" + 
				"	private class Private {\n" + 
				"		private void foo(){}\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	void foo(Foo.Private p) {\n" + 
			"	         ^^^^^^^^^^^\n" + 
			"The type Foo.Private is not visible\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	p.foo();\n" + 
			"	^\n" + 
			"The type Foo.Private is not visible\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 13)\n" + 
			"	private void foo(){}\n" + 
			"	             ^^^^^\n" + 
			"The method foo() from the type Foo.Private is never used locally\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test074() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	bar1().foo();\n" + 
				"	^^^^\n" + 
				"The method bar1() from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 6)\n" + 
				"	bar3(null);\n" + 
				"	^^^^\n" + 
				"The method bar3(Zork) from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 7)\n" + 
				"	bar4(null,null);\n" + 
				"	^^^^\n" + 
				"The method bar4(Zork) from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 9)\n" + 
				"	Zork<String> bar1() {}\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	Zork<String> bar1() {}\n" + 
				"	     ^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	List<Zork> bar2() {}\n" + 
				"	     ^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 10)\n" + 
				"	List<Zork> bar2() {}\n" + 
				"	     ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 11)\n" + 
				"	void bar3(Zork<String> z) {}\n" + 
				"	          ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"9. ERROR in X.java (at line 11)\n" + 
				"	void bar3(Zork<String> z) {}\n" + 
				"	               ^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"10. ERROR in X.java (at line 12)\n" + 
				"	void bar4(Zork<String,String> z) {}\n" + 
				"	          ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"11. ERROR in X.java (at line 12)\n" + 
				"	void bar4(Zork<String,String> z) {}\n" + 
				"	               ^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n"			
		: 		"----------\n" + 
				"1. ERROR in X.java (at line 4)\n" + 
				"	bar1().foo();\n" + 
				"	^^^^\n" + 
				"The method bar1() from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 5)\n" + 
				"	bar2();\n" + 
				"	^^^^\n" + 
				"The method bar2() from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 6)\n" + 
				"	bar3(null);\n" + 
				"	^^^^\n" + 
				"The method bar3(Zork<String>) from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 7)\n" + 
				"	bar4(null,null);\n" + 
				"	^^^^\n" + 
				"The method bar4(Zork<String,String>) from the type X refers to the missing type Zork\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 9)\n" + 
				"	Zork<String> bar1() {}\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 10)\n" + 
				"	List<Zork> bar2() {}\n" + 
				"	     ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 11)\n" + 
				"	void bar3(Zork<String> z) {}\n" + 
				"	          ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"8. ERROR in X.java (at line 12)\n" + 
				"	void bar4(Zork<String,String> z) {}\n" + 
				"	          ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.util.List;\n" + 
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		bar1().foo();\n" + 
				"		bar2();\n" + 
				"		bar3(null);\n" + 
				"		bar4(null,null);\n" + 
				"	}\n" + 
				"	Zork<String> bar1() {}\n" + 
				"	List<Zork> bar2() {}\n" + 
				"	void bar3(Zork<String> z) {}\n" + 
				"	void bar4(Zork<String,String> z) {}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test075() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	     ^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	               ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	                    ^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number>) o;\n" + 
				"	            ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number>) o;\n" + 
				"	                 ^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n"			
		: 		"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: Unchecked cast from Object to Zork<?,? extends Number>\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"	               ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: Unchecked cast from Object to Zork<?,? extends Number>\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Zork<capture#3-of ?,capture#4-of ? extends Number> cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number>) o;\n" + 
				"	            ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(Object o) {\n" + 
				"		Zork<?,?> z = (Zork<?, ? extends Number>) o;\n" + 
				"		String s = (Zork<?, ? extends Number>) o;\n" + 
				"	}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test076() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	     ^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	               ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	                    ^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? super Number>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? super Number>) o;\n" + 
				"	            ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? super Number>) o;\n" + 
				"	                 ^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n"
		: 		"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: Unchecked cast from Object to Zork<?,? super Number>\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"	               ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? super Number>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: Unchecked cast from Object to Zork<?,? super Number>\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? super Number>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Zork<capture#3-of ?,capture#4-of ? super Number> cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? super Number>) o;\n" + 
				"	            ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(Object o) {\n" + 
				"		Zork<?,?> z = (Zork<?, ? super Number>) o;\n" + 
				"		String s = (Zork<?, ? super Number>) o;\n" + 
				"	}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test077() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	     ^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	               ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	                    ^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	            ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	                 ^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Syntax error, parameterized types are only available if source level is 1.5\n" + 
				"----------\n"			
		: 		"----------\n" + 
				"1. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. WARNING in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: Unchecked cast from Object to Zork<?,? super Number[]>\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 3)\n" + 
				"	Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"	               ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"4. WARNING in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Type safety: Unchecked cast from Object to Zork<?,? extends Number[]>\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Zork<capture#3-of ?,capture#4-of ? extends Number[]> cannot be resolved to a type\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 4)\n" + 
				"	String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	            ^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n";
	
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	void foo(Object o) {\n" + 
				"		Zork<?,?> z = (Zork<?, ? super Number[]>) o;\n" + 
				"		String s = (Zork<?, ? extends Number[]>) o;\n" + 
				"	}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967
public void test078() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.util.List;\n" + 
				"interface B {\n" + 
				"  B m(String seq);\n" + 
				"}\n" + 
				"public class X implements B {\n" + 
				"	public Zork m(String arg0) {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	public Zork m(String arg0) {\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967 - variation
public void test079() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X {\n" + 
				"	public Zork m(X x) {\n" + 
				"		return x;\n" + 
				"	}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public Zork m(X x) {\n" + 
			"	       ^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967 - variation
public void test080() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"import java.util.List;\n" + 
				"interface B {\n" + 
				"  void m() throws Exception;\n" + 
				"}\n" + 
				"public class X implements B {\n" + 
				"	public void m() throws IOException {\n" + 
				"	}\n" + 
				"}\n",//-----------------------------------------------------------------------
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	public void m() throws IOException {\n" + 
			"	                       ^^^^^^^^^^^\n" + 
			"IOException cannot be resolved to a type\n" + 
			"----------\n");
}
}
