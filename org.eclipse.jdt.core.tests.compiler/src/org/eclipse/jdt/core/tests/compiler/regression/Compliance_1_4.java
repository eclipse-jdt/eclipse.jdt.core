/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.index.IIndexerOutput;
import org.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;

public class Compliance_1_4 extends AbstractRegressionTest {
public Compliance_1_4(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.4
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);	
	return options;
}
public static Test suite() {
	return setupSuite(testClass());
}
public void test01() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	class M { \n"+
			"	} \n"+
			"	void foo(){ \n"+
			"		class Y extends Secondary { \n"+
			"			M m; \n"+
			"		}; \n"+
			"		System.out.println(\"SUCCESS\");	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	class M {} \n" +
			"} \n"
		},
		"SUCCESS");
}
public void test02() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar() { \n"+
			"		return \"FAILED\";	\n" +
			"	} \n"+
			"	void foo(){ \n"+
			"		class Y extends Secondary { \n"+
			"			String z = bar();	\n" +
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	String bar(){ return \"SUCCESS\"; } \n" +
			"} \n"
		},
		"SUCCESS");
}
public void test03() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar = \"FAILED\";"+
			"	void foo(){ \n"+
			"		class Y extends Secondary { \n"+
			"			String z = bar; \n"+
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	String bar = \"SUCCESS\"; \n" +
			"} \n"
		},
		"SUCCESS");
}
public void test04() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar() { \n"+
			"		return \"SUCCESS\";	\n" +
			"	} \n"+
			"	void foo(){ \n"+
			"		class Y extends Secondary { \n"+
			"			String z = bar();	\n" +
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	private String bar(){ return \"FAILED\"; } \n" +
			"} \n"
		},
		"SUCCESS");
}
public void test05() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar = \"SUCCESS\";"+
			"	void foo(){ \n"+
			"		class Y extends Secondary { \n"+
			"			String z = bar; \n"+
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	private String bar = \"FAILED\"; \n" +
			"} \n"
		},
		"SUCCESS");
}
public void test06() {
	this.runNegativeTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		new Test().foo(); \n"+
			"	} \n"+
			"	String bar() { \n"+
			"		return \"FAILED\";	\n" +
			"	} \n"+
			"	void foo(){ \n"+
			"		class Y extends Secondary { \n"+
			"			String z = bar();	\n" +
			"		}; \n"+
			"		System.out.println(new Y().z);	\n" +
			"	} \n"+
			"} \n" +
			"class Secondary { \n" +
			"	String bar(int i){ return \"SUCCESS\"; } \n" +
			"} \n"
		},
		"----------\n" + 
		"1. ERROR in p1\\Test.java (at line 11)\n" + 
		"	String z = bar();	\n" + 
		"	           ^^^\n" + 
		"The method bar(int) in the type Secondary is not applicable for the arguments ()\n" + 
		"----------\n"
	);
}
public void test07() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		try {	\n" +
			"			throw null; \n"+
			"		} catch(NullPointerException e){ 	\n" +
			"			System.out.println(\"SUCCESS\");	\n"	+
			"		}	\n" +
			"	} \n"+
			"} \n"
		},
		"SUCCESS");
}
public void test08() {
	this.runNegativeTest(
		new String[] {
			"p1/Test.java",
			"package p1; \n"+
			"import Test2;	\n" +
			"import Test2.Member;	\n" +
			"public class Test { \n"+
			"	public static void main(String[] arguments) { \n"+
			"	} \n"+
			"} \n",
			"Test2.java",
			"public class Test2 { \n"+
			"	public class Member {	\n" +
			"	} \n"+
			"} \n"
		},
		"----------\n" + 
		"1. ERROR in p1\\Test.java (at line 2)\n" + 
		"	import Test2;	\n" + 
		"	       ^^^^^\n" + 
		"The import Test2 cannot be resolved\n" + 
		"----------\n" + 
		"2. ERROR in p1\\Test.java (at line 3)\n" + 
		"	import Test2.Member;	\n" + 
		"	       ^^^^^^^^^^^^\n" + 
		"The import Test2 cannot be resolved\n" + 
		"----------\n"
	);
}
// binary compatibility
public void test09() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1; \n"+
			"public class Z {	\n" +
			"	public static void main(String[] arguments) { \n"+
			"		Y y = new Y();	\n" +
			"		System.out.print(y.field);	\n"	+
			"		System.out.print(y.staticField);	\n"	+
			"		System.out.print(y.method());	\n"	+
			"		System.out.println(y.staticMethod());	\n"	+
			"	} \n"+
			"} \n",
			"p1/X.java",
			"package p1; \n"+
			"public class X { \n"+
			"	public String field = \"X.field-\";	\n" +
			"	public static String staticField = \"X.staticField-\";	\n" +
			"	public String method(){ return \"X.method()-\";	}	\n" +
			"	public static String staticMethod(){ return \"X.staticMethod()-\";	}	\n" +
			"} \n",
			"p1/Y.java",
			"package p1; \n"+
			"public class Y extends X { \n"+
			"} \n"
		},
		"X.field-X.staticField-X.method()-X.staticMethod()-");

	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1; \n"+
			"public class Y extends X { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		Z.main(arguments);	\n" +
			"	}	\n" +
			"	public String field = \"Y.field-\";	\n" +
			"	public static String staticField = \"Y.staticField-\";	\n" +
			"	public String method(){ return \"Y.method()-\";	}	\n" +
			"	public static String staticMethod(){ return \"Y.staticMethod()-\";	}	\n" +
			"} \n"
		},
		"Y.field-Y.staticField-Y.method()-Y.staticMethod()-", // expected output
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

// check actualReceiverType when array type
public void test10() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1; \n"+
			"public class Z {	\n" +
			"	public static void main(String[] arguments) { \n"+
			"		String[] s = new String[]{\"SUCCESS\" };	\n" +
			"		System.out.print(s.length);	\n"	+
			"		System.out.print(((String[])s.clone())[0]);	\n"	+
			"	} \n"+
			"} \n"
		},
		"1SUCCESS");
}
// test unreachable code complaints
public void test11() {
	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1; \n"+
			"public class X { \n"+
			"	void foo() { \n"+
			"		while (false);	\n" +
			"		while (false) System.out.println(\"unreachable\");	\n" +
			"		do ; while (false);	\n" +
			"		do System.out.println(\"unreachable\"); while (false);	\n" +
			"		for (;false;);	\n" +
			"		for (;false;) System.out.println(\"unreachable\");	\n" +
			"		if (false);	\n" +
			"		if (false)System.out.println(\"unreachable\");		\n" +		
			"	}	\n" +
			"} \n"
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 4)\n" + 
		"	while (false);	\n" + 
		"	             ^\n" + 
		"Unreachable code\n" + 
		"----------\n" + 
		"2. ERROR in p1\\X.java (at line 5)\n" + 
		"	while (false) System.out.println(\"unreachable\");	\n" + 
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n" + 
		"3. ERROR in p1\\X.java (at line 8)\n" + 
		"	for (;false;);	\n" + 
		"	             ^\n" + 
		"Unreachable code\n" + 
		"----------\n" + 
		"4. ERROR in p1\\X.java (at line 9)\n" + 
		"	for (;false;) System.out.println(\"unreachable\");	\n" + 
		"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unreachable code\n" + 
		"----------\n"
	);
}
// binary compatibility
public void test12() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1;	\n" +
			"class Store {	\n" +
			"	String value;	\n" +
			"	Store(String value){	\n" +
			"		this.value = value;	\n" +
			"	}	\n" +
			"}	\n" +
			"class Top {	\n" +
			"	static String bar = \"Top.bar\";	\n" +
			"	String foo = \"Top.foo\";	\n" +
			"	Store store = new Store(\"Top.store\");	\n" +
			"	static Store sstore = new Store(\"Top.sstore\");	\n" +
			"	static Top ss = new Top();	\n" +
			"}	\n" +
			"public class Y extends Updated {		\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new Y().test();	\n" +
			"	}	\n" +
			"	void test() {		\n" +
			"		System.out.print(\"*** FIELD ACCESS ***\");	\n" +
			"		System.out.print(\"*1* new Updated().bar: \" + new Updated().bar);	\n" +
			"		System.out.print(\"*2* new Updated().foo: \" + new Updated().foo);	\n" +
			"		System.out.print(\"*3* new Y().foo: \" + new Y().foo);	\n" +
			"		System.out.print(\"*4* new Y().bar: \" + new Y().bar);	\n" +
			"		System.out.print(\"*5* bar: \" + bar);	\n" +
			"		System.out.print(\"*6* foo: \" + foo);	\n" +
			"		System.out.print(\"*7* Y.bar: \" + Y.bar);	\n" +
			"		System.out.print(\"*8* this.bar: \" + this.bar);	\n" +
			"		System.out.print(\"*9* this.foo: \" + this.foo);	\n" +
			"		System.out.print(\"*10* store.value: \" + store.value);	\n" +
			"		System.out.print(\"*11* sstore.value: \" + sstore.value);	\n" +
			"		System.out.print(\"*12* ss.sstore.value: \" + ss.sstore.value);	\n" +
			"	}		\n" +
			"}		\n",
			"p1/Updated.java",
			"package p1;	\n" +
			"public class Updated extends Top {	\n" +
			"}	\n"
		},
		"*** FIELD ACCESS ***"
		+"*1* new Updated().bar: Top.bar"
		+"*2* new Updated().foo: Top.foo"
		+"*3* new Y().foo: Top.foo"
		+"*4* new Y().bar: Top.bar"
		+"*5* bar: Top.bar"
		+"*6* foo: Top.foo"
		+"*7* Y.bar: Top.bar"
		+"*8* this.bar: Top.bar"
		+"*9* this.foo: Top.foo"
		+"*10* store.value: Top.store"
		+"*11* sstore.value: Top.sstore"
		+"*12* ss.sstore.value: Top.sstore");

	this.runConformTest(
		new String[] {
			"p1/Updated.java",
			"package p1; \n"+
			"public class Updated extends Top { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		Y.main(arguments);	\n" +
			"	}	\n" +
			"	static String bar = \"Updated.bar\";	\n" +
			"	String foo = \"Updated.foo\";	\n" +
			"	Store store = new Store(\"Updated.store\");	\n" +
			"	static Store sstore = new Store(\"Updated.sstore\");	\n" +
			"	static Updated ss = new Updated();	\n" +
			"} \n"
		},
		"*** FIELD ACCESS ***"
		+"*1* new Updated().bar: Updated.bar"
		+"*2* new Updated().foo: Updated.foo"
		+"*3* new Y().foo: Updated.foo"
		+"*4* new Y().bar: Updated.bar"
		+"*5* bar: Top.bar"
		+"*6* foo: Updated.foo"
		+"*7* Y.bar: Updated.bar"
		+"*8* this.bar: Updated.bar"
		+"*9* this.foo: Updated.foo"
		+"*10* store.value: Updated.store"
		+"*11* sstore.value: Top.sstore"
		+"*12* ss.sstore.value: Top.sstore",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args		
}
// binary compatibility
public void test13() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"package p1;	\n" +
			"class Store {	\n" +
			"	String value;	\n" +
			"	Store(String value){	\n" +
			"		this.value = value;	\n" +
			"	}	\n" +
			"}	\n" +
			"class Top {	\n" +
			"	static String bar() { return \"Top.bar()\"; }	\n" +
			"	String foo() { return \"Top.foo()\"; }	\n" +
			"}	\n" +
			"public class Y extends Updated {		\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new Y().test();	\n" +
			"	}	\n" +
			"	void test() {		\n" +
			"		System.out.print(\"*** METHOD ACCESS ***\");	\n" +
			"		System.out.print(\"*1* new Updated().bar(): \" + new Updated().bar());	\n" +
			"		System.out.print(\"*2* new Updated().foo(): \" + new Updated().foo());	\n" +
			"		System.out.print(\"*3* new Y().foo(): \" + new Y().foo());	\n" +
			"		System.out.print(\"*4* new Y().bar(): \" + new Y().bar());	\n" +
			"		System.out.print(\"*5* bar(): \" + bar());	\n" +
			"		System.out.print(\"*6* foo(): \" + foo());	\n" +
			"		System.out.print(\"*7* Y.bar(): \" + Y.bar());	\n" +
			"		System.out.print(\"*8* this.bar(): \" + this.bar());	\n" +
			"		System.out.print(\"*9* this.foo(): \" + this.foo());	\n" +
			"	}		\n" +
			"}		\n",
			"p1/Updated.java",
			"package p1;	\n" +
			"public class Updated extends Top {	\n" +
			"}	\n"
		},
		"*** METHOD ACCESS ***"
		+"*1* new Updated().bar(): Top.bar()"
		+"*2* new Updated().foo(): Top.foo()"
		+"*3* new Y().foo(): Top.foo()"
		+"*4* new Y().bar(): Top.bar()"
		+"*5* bar(): Top.bar()"
		+"*6* foo(): Top.foo()"
		+"*7* Y.bar(): Top.bar()"
		+"*8* this.bar(): Top.bar()"
		+"*9* this.foo(): Top.foo()");

	this.runConformTest(
		new String[] {
			"p1/Updated.java",
			"package p1; \n"+
			"public class Updated extends Top { \n"+
			"	public static void main(String[] arguments) { \n"+
			"		Y.main(arguments);	\n" +
			"	}	\n" +
			"	static String bar() { return \"Updated.bar()\"; }	\n" +
			"	String foo() { return \"Updated.foo()\"; }	\n" +
			"} \n"
		},
		"*** METHOD ACCESS ***"
		+"*1* new Updated().bar(): Updated.bar()"
		+"*2* new Updated().foo(): Updated.foo()"
		+"*3* new Y().foo(): Updated.foo()"
		+"*4* new Y().bar(): Updated.bar()"
		+"*5* bar(): Top.bar()"
		+"*6* foo(): Updated.foo()"
		+"*7* Y.bar(): Updated.bar()"
		+"*8* this.bar(): Updated.bar()"
		+"*9* this.foo(): Updated.foo()",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args		
}

public void test14() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"class T {	\n" +
			"	void foo(boolean b) {	\n" +
			"		 System.out.print(\"T.foo(boolean)#\"); 	\n" +
			"	}	\n" +
			"	boolean bar = false;	\n" +
			"	class Member {	\n" +
			"		void display(){ System.out.print(\"T.Member#\"); }	\n" +
			"	}	\n" +
			"}	\n" +
			"public class X {	\n" +
			"	void foo(int i) {	\n" +
			"		 System.out.println(\"X.foo(int)#\"); 			\n" +
			"	}	\n" +
			"	int bar;	\n" +
			"	class Member {	\n" +
			"		void display(){ System.out.print(\"X.Member#\"); }	\n" +
			"	}	\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new X().bar();	\n" +
			"	}				\n" +
			"	void bar() { 	\n" +
			"		new T() {	\n" +
			"			{	\n" +
			"				foo(true);	\n" +
			"				System.out.print((boolean)bar + \"#\");	\n" +
			"				Member m = new Member();	\n" +	
			"				m.display();	\n" +
			"			} 	\n" +
			"		};	\n" +
			"	}	\n" +
			"}	\n"
		},
		"T.foo(boolean)#false#T.Member");
}

/*
 * check handling of default abstract methods
 */
public void test15() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		C c = new C() {	\n"+
			"			public void doSomething(){	\n"+
			"				System.out.println(\"SUCCESS\");	\n"+
			"			}	\n"+
			"		};	\n"+
			"		c.doSomething();	\n"+
			"	}	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void doSomething();	\n"+
			"}	\n"+
			"abstract class C implements I {	\n"+
			"}	\n"
		},
		"SUCCESS");
}
			
public void test16() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(boolean b) {}	\n"+
			"}	\n"+
			"public class X {	\n"+
			"      void foo(int i) {}	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"} 	\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	foo(0); 	\n" + 
		"	^^^\n" + 
		"The method foo(boolean) in the type T is not applicable for the arguments (int)\n" + 
		"----------\n");
}

public void test17() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(boolean b) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"public class X {	\n"+
			"      void foo(int i) {}	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(false); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}

public void test18() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(int j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"public class X {	\n"+
			"      void foo(int i) {}	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
public void test19() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(int j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"class U {	\n"+
			"      void foo(int j) { System.out.println(\"FAILED\"); }	\n"+
			"}	\n"+
			"public class X extends U {	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
public void test20() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class T {	\n"+
			"      void foo(int j) { System.out.println(\"SUCCESS\"); }	\n"+
			"}	\n"+
			"class U {	\n"+
			"      void foo(boolean j) { System.out.println(\"FAILED\"); }	\n"+
			"}	\n"+
			"public class X extends U {	\n"+
			"      void bar() {	\n"+
			"            new T() {	\n"+
			"                  {	\n"+
			"                        foo(0); 	\n"+
			"                  }	\n"+
			"            };	\n"+
			"      }	\n"+
			"      public static void main(String[] arguments) {	\n"+
			"			new X().bar();	\n" +
			"      }	\n"+
			"} 	\n"
		},
		"SUCCESS");
}
// binary check for 11511
public void test21() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"package p1;	\n" +
			"public class Z extends AbstractA {	\n" +
			"	public static void main(String[] arguments) {	\n" +
			"		new Z().init(); 	\n" +
			"	}	\n" +
			"}	\n" +
			"abstract class AbstractB implements K {	\n" +
			"	public void init() {	\n" +
			"		System.out.println(\"AbstractB.init()\");	\n" +
			"	}	\n" +
			"}	\n" +
			"interface K {	\n" +
			"	void init();	\n" +
			"	void init(int i);	\n" +
			"}	\n",
			"p1/AbstractA.java",
			"package p1;	\n" +
			"public abstract class AbstractA extends AbstractB implements K {	\n" +
			"	public void init(int i) {	\n" +
			"	}	\n" +
			"}	\n"			
		},
		"AbstractB.init()"); // no special vm args			

		// check that "new Z().init()" is bound to "Z.init()"
		final StringBuffer references = new StringBuffer(10);
		try {
			BinaryIndexer indexer = new BinaryIndexer(true);
			indexer.index(
				new IDocument() {
					public byte[] getByteContent() throws IOException {
						return Util.getFileByteContent(new File(OUTPUT_DIR + "/p1/Z.class"));
					}
					public char[] getCharContent() throws IOException { return null; }
					public String getName() { return "Z.class"; }
					public String getProperty(String property) { return null; }
					public Enumeration getPropertyNames() { return null; }
					public String getStringContent() throws IOException { return null; }
					public String getType() { return "class"; }
					public void setProperty(String attribute, String value) { }
					public String getEncoding() { return null; }
				}, 
				new IIndexerOutput() {
					public void addDocument(IDocument document) { }
					public void addRef(char[] word) { 
						references.append(word);
						references.append('\n');
					}
					public void addRef(String word) {
						//System.out.println(word);
					}
				});
		} catch(IOException e) {
		}
		String computedReferences = references.toString();
		boolean check = computedReferences.indexOf("constructorRef/Z/0\nmethodRef/init/0") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not bind 'new Z().init()' to Z.init()'", check);
}
 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test22() {

	this.runNegativeTest(
		new String[] {
			"p1/T.java",
			"package p1;	\n"+
			"interface II {}	\n"+
			"class TT {	\n"+
			"	void foo(boolean b) {}	\n"+
			"	void foo(int i, boolean b) {}	\n"+
			"	void foo(String s) {}	\n"+
			"}	\n"+
			"public abstract class T implements II {	\n"+
			"	void foo(int i) {}	\n"+
			"	void bar() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(0); // should say that foo(int, boolean) isn't applicable	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void boo() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(true); // should not complain about ambiguity	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"----------\n" + 
		"1. ERROR in p1\\T.java (at line 13)\n" + 
		"	foo(0); // should say that foo(int, boolean) isn\'t applicable	\n" + 
		"	^^^\n" + 
		"The method foo(int, boolean) in the type TT is not applicable for the arguments (int)\n" + 
		"----------\n");
}
   
 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test23() {

	this.runNegativeTest(
		new String[] {
			"p1/T.java",
			"package p1;	\n"+
			"interface II {}	\n"+
			"abstract class TT {	\n"+		// 259+ABSTRACT
			"	void foo(boolean b) {}	\n"+
			"	void foo(int i, boolean b) {}	\n"+
			"	void foo(String s) {}	\n"+
			"}	\n"+
			"public abstract class T implements II {	\n"+
			"	void foo(int i) {}	\n"+
			"	void bar() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(0); // should say that foo(int, boolean) isn't applicable	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void boo() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(true); // should complain ambiguity	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"----------\n" + 
		"1. ERROR in p1\\T.java (at line 13)\n" + 
		"	foo(0); // should say that foo(int, boolean) isn\'t applicable	\n" + 
		"	^^^\n" + 
		"The method foo(int, boolean) in the type TT is not applicable for the arguments (int)\n" + 
		"----------\n");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test24() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"interface II {}	\n"+
			"abstract class T implements II {	\n"+
			"	void foo(boolean b) {}	\n"+
			"	void foo(int i, boolean b) {}	\n"+
			"}	\n"+
			"abstract class TT implements II {	\n"+
			"	void foo(boolean b) {}	\n"+
			"}	\n"+
			"public class X {	\n"+
			"	void foo(int i) {}	\n"+
			"	void bar() {	\n"+
			"		new T() {	\n"+
			"			{	\n"+
			"				foo(0); // javac says foo cannot be resolved because of multiple matches	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void bar2() {	\n"+
			"		new TT() {	\n"+
			"			{	\n"+
			"				foo(0); // should say that foo(boolean) isn't applicable	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"	void boo() {	\n"+
			"		new T() {	\n"+
			"			{	\n"+
			"				foo(true); // should complain ambiguity	\n"+
			"			}	\n"+
			"		};	\n"+
			"	}	\n"+
			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 15)\n" + 
		"	foo(0); // javac says foo cannot be resolved because of multiple matches	\n" + 
		"	^^^\n" + 
		"The method foo(int, boolean) in the type T is not applicable for the arguments (int)\n" + 
		"----------\n" + 
		"2. ERROR in p1\\X.java (at line 22)\n" + 
		"	foo(0); // should say that foo(boolean) isn\'t applicable	\n" + 
		"	^^^\n" + 
		"The method foo(boolean) in the type TT is not applicable for the arguments (int)\n" + 
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis (no matter if super is abstract or not)
 */
public void test25() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X extends AbstractY {	\n"+
			"	void bar(){	\n"+
			"		init(\"hello\");	\n"+
			"	}		\n"+
			"}	\n"+
			"abstract class AbstractY implements I {	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void init(String s, int i);	\n"+
			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 2)\n" + 
		"	public class X extends AbstractY {	\n" + 
		"	             ^\n" + 
		"Class must implement the inherited abstract method I.init(String, int)\n" + 
		"----------\n" + 
		"2. ERROR in p1\\X.java (at line 4)\n" + 
		"	init(\"hello\");	\n" + 
		"	^^^^\n" + 
		"The method init(String, int) in the type I is not applicable for the arguments (String)\n" + 
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis (no matter if super is abstract or not)
 */
public void test26() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X extends AbstractY {	\n"+
			"	void bar(){	\n"+
			"		init(\"hello\");	\n"+
			"	}		\n"+
			"}	\n"+
			"class AbstractY implements I {	\n"+
			"}	\n"+
			"interface I {	\n"+
			"	void init(String s, int i);	\n"+
			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 4)\n" + 
		"	init(\"hello\");	\n" + 
		"	^^^^\n" + 
		"The method init(String, int) in the type I is not applicable for the arguments (String)\n" + 
		"----------\n" + 
		"2. ERROR in p1\\X.java (at line 7)\n" + 
		"	class AbstractY implements I {	\n" + 
		"	      ^^^^^^^^^\n" + 
		"Class must implement the inherited abstract method I.init(String, int)\n" + 
		"----------\n"
);
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11922
 * should report unreachable empty statement
 */
public void test27() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		for (;false;p());	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n"+
			"	static void p(){	\n"+
			"		System.out.println(\"FAILED\");	\n"+
			"	}	\n"+
			"}	\n"
		},
		"----------\n" + 
		"1. ERROR in p1\\X.java (at line 4)\n" + 
		"	for (;false;p());	\n" + 
		"	                ^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=12445
 * should report unreachable empty statement
 */
public void test28() {

	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"interface FooInterface {	\n" +
			"	public boolean foo(int a);	\n" +
			"	public boolean bar(int a);	\n" +
			"}	\n" +
			"public class X extends Z {	\n" +
			"	public boolean foo(int a){ return true; }	\n" +
			"	public boolean bar(int a){ return false; }	\n" +
			"	public static void main(String[] arguments) {	\n"+
			"		System.out.println(new X().test(0));	\n"+
			"	}	\n" +
			"}\n" +
			"abstract class Z implements FooInterface {	\n" +
			"	public boolean foo(int a, int b) {	\n" +
			"		return true;	\n" +
			"	}	\n" +
			"	public String test(int a) {	\n" +
			"		boolean result = foo(a); \n" +
			"		if (result)	\n" +
			"			return \"SUCCESS\";	\n" +
			"		else	\n" +
			"			return \"FAILED\";	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * verify error on qualified name ref in 1.4
 */
public void test29() {

	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public static void main(String[] args) {	\n" +
			"		new X();	\n" +
			"		System.out.println(\"SUCCESS\");	\n" +
			"	}  	\n" +
			"	Woof woof_1;	\n" +
			"	public class Honk {	\n" +
			"		Integer honks;	\n" +
			"	}	\n" +
			"	public class Meow {	\n" +
			"		Honk honk_1;	\n" +
			"	}	\n" +
			"	public class Woof {	\n" +
			"		Meow meow_1;	\n" +
			"	}	\n" +
			"	public void setHonks(int num) {	\n" +
			"		// This is the line that causes the VerifyError	\n" +
			"		woof_1.meow_1.honk_1.honks = new Integer(num);	\n" +
			"		// Here is equivalent code that does not cause the error.	\n" +
			"		//  Honk h = woof_1.moo_1.meow_1.honk_1;	\n" +
			"		//  h.honks = new Integer(num);	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected method implementations.
 */
public void test30() {
	
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public static void main(String[] args){	\n" +
			"		new q.X2().foo(\"String\");	\n" +
			"		new q.X2().bar(\"String\");	\n" +
			"		new q.X2().barbar(\"String\");	\n" +
			"		new q.X2().baz(\"String\");	\n" +
			"	}	\n" +
			"}	\n",
			
			"p/X1.java",
			"package p;	\n" +
			"public abstract class X1 {	\n" +
			"	protected void foo(Object o){	System.out.println(\"X1.foo(Object)\"); }	\n" +
			"	protected void bar(Object o){	System.out.println(\"X1.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X1.barbar(Object)\"); }	\n" +
			"	protected void baz(Object o) { System.out.println(\"X1.baz(Object)\"); }	\n" +
			"}	\n",
			
			"q/X2.java",
			"package q;	\n" +
			"public class X2 extends p.X1 {	\n" +
			"	protected void foo(int i) { System.out.println(\"X2.foo(int)\"); }	\n" +
			"	protected void bar(Object o) { System.out.println(\"X2.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" +
			"	protected void baz(String s) {	System.out.println(\"X2.baz(String)\"); }	\n" +
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 5)\n" + 
		"	new q.X2().bar(\"String\");	\n" + 
		"	           ^^^\n" + 
		"The method bar(String) from the type X2 is not visible\n" + 
		"----------\n" + 
		"2. ERROR in p\\X.java (at line 6)\n" + 
		"	new q.X2().barbar(\"String\");	\n" + 
		"	           ^^^^^^\n" + 
		"The method barbar(String) from the type X2 is not visible\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in q\\X2.java (at line 5)\n" + 
		"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" + 
		"	     ^^^^^^^^^^^^^^^^\n" + 
		"The method X2.barbar(Object) does not override the inherited method from X1 since it is private to a different package.\n" + 
		"----------\n");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected method implementations.
 */
public void test31() {
	
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X extends q.X2 {	\n" +
			"	public static void main(String[] args){	\n" +
			"			new X().doSomething();	\n" +
			"	}	\n" +
			"	void doSomething(){	\n" +
			"		foo(\"String\");	\n" +
			"		bar(\"String\");	\n" +
			"		barbar(\"String\");	\n" +
			"		baz(\"String\");	\n" +
			"	}	\n" +
			"}	\n",
			
			"p/X1.java",
			"package p;	\n" +
			"public abstract class X1 {	\n" +
			"	protected void foo(Object o){	System.out.println(\"X1.foo(Object)\"); }	\n" +
			"	protected void bar(Object o){	System.out.println(\"X1.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X1.barbar(Object)\"); }	\n" +
			"	protected void baz(Object o) { System.out.println(\"X1.baz(Object)\"); }	\n" +
			"}	\n",
			
			"q/X2.java",
			"package q;	\n" +
			"public class X2 extends p.X1 {	\n" +
			"	protected void foo(int i) { System.out.println(\"X2.foo(int)\"); }	\n" +
			"	protected void bar(Object o) { System.out.println(\"X2.bar(Object)\"); }	\n" +
			"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" +
			"	protected void baz(String s) {	System.out.println(\"X2.baz(String)\"); }	\n" +
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 9)\n" + 
		"	barbar(\"String\");	\n" + 
		"	^^^^^^\n" + 
		"The method barbar(String) from the type X2 is not visible\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in q\\X2.java (at line 5)\n" + 
		"	void barbar(Object o){	System.out.println(\"X2.barbar(Object)\"); }	\n" + 
		"	     ^^^^^^^^^^^^^^^^\n" + 
		"The method X2.barbar(Object) does not override the inherited method from X1 since it is private to a different package.\n" + 
		"----------\n"
);
}
			
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected field implementations.
 */
public void test32() {
	
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n" +
			"public class X {	\n" +
			"	public static void main(String[] args){	\n" +
			"		System.out.println(new q.X2().foo);	\n" +
			"		System.out.println(new q.X2().bar);	\n" +
			"	}	\n" +
			"}	\n",
			
			"p/X1.java",
			"package p;	\n" +
			"public abstract class X1 {	\n" +
			"	protected String foo = \"X1.foo\"; 	\n" +
			"	String bar = \"X1.bar\";	\n" +
			"}	\n",
			
			"q/X2.java",
			"package q;	\n" +
			"public class X2 extends p.X1 {	\n" +
			"	protected String foo = \"X2.foo\";	\n" +
			"	String bar = \"X2.bar\";	\n" +
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 4)\n" + 
		"	System.out.println(new q.X2().foo);	\n" + 
		"	                              ^^^\n" + 
		"The field foo is not visible\n" + 
		"----------\n" + 
		"2. ERROR in p\\X.java (at line 5)\n" + 
		"	System.out.println(new q.X2().bar);	\n" + 
		"	                              ^^^\n" + 
		"The field bar is not visible\n" + 
		"----------\n");
}

/*
 * Initialization of synthetic fields prior to super constructor call
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23075
 */
public void test33() {

	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {	\n"+
			"  public int m;	\n"+
			"  public void pp() {	\n"+
			"     C c = new C(4);	\n"+
			"     System.out.println(c.get());	\n"+
			"  }	\n"+
			"  public static void main(String[] args) {	\n"+
			"     A a = new A();	\n"+
			"	  try {	\n"+
			"       a.pp(); 	\n"+
			"		System.out.println(\"SyntheticInit BEFORE SuperConstructorCall\");	\n"+
			"	  } catch(NullPointerException e) {	\n"+
			"		System.out.println(\"SyntheticInit AFTER SuperConstructorCall\"); // should no longer occur with target 1.4 \n"+
			"	  }	\n"+
			"  }	\n"+
			"  class C extends B {	\n"+
			"    public C(int x1) {	\n"+
			"      super(x1);    	\n"+
			"    }	\n"+
			"    protected void init(int x1) {	\n"+
			"       x = m * x1; // <- NULL POINTER EXCEPTION because of m	\n"+
			"    }  	\n"+
			"  }	\n"+
			"}	\n"+
			"class B {	\n"+
			"  int x;	\n"+
			"  public B(int x1) {	\n"+
			"    init(x1);	\n"+
			"  }	\n"+
			"  protected void init(int x1) {	\n"+
			"    x  = x1;	\n"+
			"  }	\n"+
			"  public int get() {	\n"+
			"    return x;	\n"+
			"  }	\n"+
			"}	\n"
		},
		"SyntheticInit BEFORE SuperConstructorCall");
}
/*
 * Initialization of synthetic fields prior to super constructor call - NPE check
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25174
 */
public void test34() {

	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		new X().new X2();	\n"+
			"	}	\n"+
			"	class X1 {	\n"+
			"		X1(){	\n"+
			"			this.baz();	\n"+
			"		}	\n"+
			"		void baz() {	\n"+
			"			System.out.println(\"-X1.baz()\");	\n"+
			"		}	\n"+
			"	}	\n"+
			"	class X2 extends X1 {	\n"+
			"		void baz() {	\n"+
			"			System.out.print(X.this==null ? \"X.this == null\" : \"X.this != null\");	\n"+
			"			X1 x1 = X.this.new X1(){	\n"+
			"				void baz(){	\n"+
			"					System.out.println(\"-X$1.baz()\");	\n"+
			"				}	\n"+
			"			};	\n"+
			"		}	\n"+
			"	}	\n"+
			"}\n",
		},
		"X.this != null-X$1.baz()");
}

public void test35() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	class Y {}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		try {	\n" +
			"			X x =null;	\n" +
			"			x.new Y();	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(NullPointerException e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS"
	);
}

public void test36() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"package p1;	\n"+
			"public class X {	\n"+
			"	class Y {}	\n"+
			"	static class Z extends Y {	\n"+
			"		Z (X x){	\n"+
			"			x.super();	\n" +
			"		}		\n"+
			"	}	\n"+
			"	public static void main(String[] arguments) {	\n"+
			"		try {	\n" +
			"			new Z(null);	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		} catch(NullPointerException e){	\n" +
			"			System.out.println(\"SUCCESS\");	\n" +
			"		}	\n" +
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS"
	);
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24744
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23096
 */
public void test37() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_TaskTags, "TODO:");
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;	\n"+
			"public class X {\n"+
			"}\n"+
			"// TODO: something"
		},
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 4)\n" + 
		"	// TODO: something\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Unexpected end of comment\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24833
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23096
 */
public void test38() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_TaskTags, "TODO:");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"// TODO: something"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	// TODO: something\n" + 
		"	^^^^^^^^^^^^^^^^^^\n" + 
		"Unexpected end of comment\n" + 
		"----------\n",
		null,
		true,
		customOptions);
}
/*
 * unreachable empty statement/block are diagnosed in 1.3
 */
public void test39() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"	public static void main(String[] args){	\n"+
			"		for (;null != null;);	\n"+
			"		for (;null != null;){}	\n"+
			"		for (;false;);	\n"+
			"		for (;false;){}	\n"+
			"		while (false);	\n"+
			"		while (false){}	\n"+
			"		if (false) {} else {}	\n"+
			"		if (false) ; else ;			\n"+
			"		System.out.println(\"FAILED\");	\n" +
			"	}	\n"+
			"}	\n",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	for (;false;);	\n" + 
		"	             ^\n" + 
		"Unreachable code\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 6)\n" + 
		"	for (;false;){}	\n" + 
		"	             ^^\n" + 
		"Unreachable code\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 7)\n" + 
		"	while (false);	\n" + 
		"	             ^\n" + 
		"Unreachable code\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 8)\n" + 
		"	while (false){}	\n" + 
		"	             ^^\n" + 
		"Unreachable code\n" + 
		"----------\n");
}

public static Class testClass() {
	return Compliance_1_4.class;
}
}
