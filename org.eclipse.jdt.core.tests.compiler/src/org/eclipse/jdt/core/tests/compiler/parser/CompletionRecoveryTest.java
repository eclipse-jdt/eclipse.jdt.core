/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

public class CompletionRecoveryTest extends AbstractCompletionTest {
public CompletionRecoveryTest(String testName){
	super(testName);
}
static {
//	TESTS_NUMBERS = new int[] { 22 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionRecoveryTest.class);
}
/*
 * Complete on variable behind ill-formed declaration
 */
public void test01() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends IOException {	\t
			int foo(){							\t
				String str = ;					\t
				str.							\t
		""";

	String completeBehind = "str.";
	String expectedCompletionNodeToString = "<CompleteOnName:str.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException {
		  public X() {
		  }
		  int foo() {
		    String str;
		    <CompleteOnName:str.>;
		  }
		}
		""";
	String expectedReplacedSource = "str.";
	String testName = "<complete on variable behind ill-formed declaration>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable behind ill-formed declaration and nested block
 */
public void test02() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends IOException {	\t
			int foo(){							\t
				String str = ;					\t
				{								\t
				 	int i;						\t
					str.						\t
		""";

	String completeBehind = "str.";
	String expectedCompletionNodeToString = "<CompleteOnName:str.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException {
		  public X() {
		  }
		  int foo() {
		    String str;
		    {
		      int i;
		      <CompleteOnName:str.>;
		    }
		  }
		}
		""";
	String expectedReplacedSource = "str.";
	String testName = "<complete on variable behind ill-formed declaration and nested block>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable behind ill-formed declaration and inside local type field initialization
 */
public void test03() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends IOException {	\t
			int foo(){							\t
				final String str = ;			\t
				class L {						\t
				 	int i = str					\t
		""";

	String completeBehind = "i = str";
	String expectedCompletionNodeToString = "<CompleteOnName:str>";
	String completionIdentifier = "str";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException {
		  public X() {
		  }
		  int foo() {
		    final String str;
		    class L {
		      int i = <CompleteOnName:str>;
		      L() {
		        super();
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "str";
	String testName = "<complete on variable behind ill-formed declaration and inside local type field initialization>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable behind closed scope
 */
public void test04() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends 				\t
			int foo(String str)					\t
				String variable = ;				\t
				{								\t
				 	String variableNotInScope;	\t
				}								\t
				foo(varia						\t
		""";

	String completeBehind = "foo(var";
	String expectedCompletionNodeToString = "<CompleteOnName:var>";
	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  public X() {
		  }
		  int foo(String str) {
		    String variable;
		    foo(<CompleteOnName:var>);
		  }
		}
		""";
	String expectedReplacedSource = "varia";
	String testName = "<complete on variable behind closed scope>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable str with sibling method stringAppend()
 */
public void test05() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends 						\t
			int foo(String str)							\t
				String str = ;							\t
				{										\t
				 	String strNotInScope;				\t
				}										\t
				class L {								\t
					int bar(){							\t
						foo(str							\t
					void stringAppend(String s1, String s2)\t
		""";

	String completeBehind = "foo(str";
	String expectedCompletionNodeToString = "<CompleteOnName:str>";
	String completionIdentifier = "str";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  public X() {
		  }
		  int foo(String str) {
		    String str;
		    class L {
		      L() {
		      }
		      int bar() {
		        foo(<CompleteOnName:str>);
		      }
		      void stringAppend(String s1, String s2) {
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "str";
	String testName = "<complete on variable str with sibling method stringAppend()>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on variable str with sibling method stringAppend(), eliminating
 * uninteresting method bodies
 */
public void test06() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends 						\t
			int foo(String str)							\t
				String str = ;							\t
				{										\t
				 	String strNotInScope;				\t
				}										\t
				class L {								\t
					int notInterestingBody(){			\t
						System.out.println();			\t
					}									\t
					int bar(){							\t
						foo(str							\t
					void stringAppend(String s1, String s2)\t
		""";

	String completeBehind = "foo(str";
	String expectedCompletionNodeToString = "<CompleteOnName:str>";
	String completionIdentifier = "str";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  public X() {
		  }
		  int foo(String str) {
		    String str;
		    class L {
		      L() {
		      }
		      int notInterestingBody() {
		      }
		      int bar() {
		        foo(<CompleteOnName:str>);
		      }
		      void stringAppend(String s1, String s2) {
		      }
		    }
		  }
		}
		""";

	String expectedReplacedSource = "str";
	String testName = "<complete on variable eliminating other uninteresting method bodies>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on new keyword
 */
public void test07(){

	String str =
		"""
		import java.io.*						\t
												\t
		public class X extends IOException {	\t
			int foo() {							\t
				X x = new X(					\t
		}										\t
		""";

	String completeBehind = "= n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException {
		  public X() {
		  }
		  int foo() {
		    X x = <CompleteOnName:n>;
		  }
		}
		""";
	String expectedReplacedSource = "new";
	String testName = "<complete on new keyword>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on field type in anonymous type.
 */
public void test08() {
	runTestCheckDietParse(
		"""
			package test;
			import java.util.Vector;
			public class VA {
				Object o1 = new Object() {
					V
					void foo2() {
						int i = 1;
					}
				};
				String s2;
				void bar() {
				}
				void foo() {\s
					new String[] {}..equals()
				}
			}
			""",
		// completeBehind:
		"		V",
		// expectedCompletionNodeToString:
		"<CompleteOnType:V>",
		"""
			package test;
			import java.util.Vector;
			public class VA {
			  Object o1 = new Object() {
			    <CompleteOnType:V>;
			    void foo2() {
			    }
			  };
			  String s2;
			  public VA() {
			  }
			  void bar() {
			  }
			  void foo() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"V",
		// expectedReplacedSource:
		"V",
		// test name
		"<completion on field type in anonymous type>"
	);
}
/*
 * Complete on argument name
 */
public void test09() {
	runTestCheckDietParse(
		"""
			package pack;							\t
			class A  {								\t
													\t
				public static void main(String[] argv\t
						new Member().f				\t
						;							\t
				}									\t
				class Member {						\t
					int foo()						\t
					}								\t
				}									\t
			};										\t
			""",
		// completeBehind:
		"argv",
		// expectedCompletionNodeToString:
		"<CompleteOnArgumentName:String[] argv>",
		"""
			package pack;
			class A {
			  class Member {
			    Member() {
			    }
			    int foo() {
			    }
			  }
			  A() {
			  }
			  public static void main(<CompleteOnArgumentName:String[] argv>) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"argv",
		// expectedReplacedSource:
		"argv",
		// test name
		"<completion on argument name>"
	);
}
/*
 * Complete on argument name
 */
public void test10() {
	this.runTestCheckMethodParse(
		"""
			package pack;							\t
			class A  {								\t
													\t
				public static void main(String[] argv\t
						new Member().f				\t
						;							\t
				}									\t
				class Member {						\t
					int foo()						\t
					}								\t
				}									\t
			};										\t
			""",
		// completeBehind:
		"argv",
		// expectedCompletionNodeToString:
		"<CompleteOnArgumentName:String[] argv>",
		"""
			package pack;
			class A {
			  class Member {
			    Member() {
			    }
			    int foo() {
			    }
			  }
			  A() {
			  }
			  public static void main(<CompleteOnArgumentName:String[] argv>) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"argv",
		// expectedReplacedSource:
		"argv",
		// test name
		"<completion on argument name>"
	);
}
/*
 * Complete inside method with incomplete signature
 */
public void test11() {
	this.runTestCheckMethodParse(
		"""
			package pack;							\t
			class A  {								\t
													\t
				public static void main(String[] argv\t
						new Member().f				\t
						;							\t
				}									\t
				class Member {						\t
					int foo()						\t
					}								\t
				}									\t
			};										\t
			""",
		// completeBehind:
		"new Member().f",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:new Member().f>",
		"""
			package pack;
			class A {
			  class Member {
			    Member() {
			    }
			    int foo() {
			    }
			  }
			  A() {
			  }
			  public static void main(String[] argv) {
			    <CompleteOnMemberAccess:new Member().f>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"f",
		// expectedReplacedSource:
		"f",
		// test name
		"<complete inside method with incomplete signature>"
	);
}
/*
 * Complete on argument name with class decl later on
 */
public void test12() {
	this.runTestCheckMethodParse(
		"""
			class DD  {								\t
				public static void main(String[] argv	\t
														\t
			class D {									\t
														\t
				int i;									\t
				class Mem1 {}							\t
				int dumb(String s)						\t
				int dumb(float fNum, double dNum) {		\t
					dumb("world", i);					\t
														\t
					if (i == 0) {						\t
						class Local {					\t
														\t
							int hello() 				\t
			""",
		// completeBehind:
		"argv",
		// expectedCompletionNodeToString:
		"<CompleteOnArgumentName:String[] argv>",
		"""
			class DD {
			  DD() {
			  }
			  public static void main(<CompleteOnArgumentName:String[] argv>) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"argv",
		// expectedReplacedSource:
		"argv",
		// test name
		"<complete on argument name with class decl later on>"
	);
}
/*
 * Complete behind array type
 */
public void test13() {
	this.runTestCheckMethodParse(
		"""
			class C {					\t
				void test() {			\t
					String[].			\t
				}						\t
			}							\t
			""",
		// completeBehind:
		"String[].",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:String[].>",
		"""
			class C {
			  C() {
			  }
			  void test() {
			    <CompleteOnClassLiteralAccess:String[].>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"String[].",
		// test name
		"<complete behind array type>"
	);
}
/*
 * Complete inside array type
 */
public void test14() {
	runTestCheckDietParse(
		"""
			public class B {		\t
				class Member {}		\t
									\t
				int[] j;			\t
			""",
		// completeBehind:
		"int[",
		// expectedCompletionNodeToString:
		NONE,
		"""
			public class B {
			  class Member {
			    Member() {
			    }
			  }
			  public B() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		NONE,
		// test name
		"<complete inside array type>"
	);
}
/*
 * Complete inside array type
 */
public void test15() {
	runTestCheckDietParse(
		"""
			public class B {		\t
				class Member {}		\t
									\t
				int[				\t
			""",
		// completeBehind:
		"int[",
		// expectedCompletionNodeToString:
		NONE,
		"""
			public class B {
			  class Member {
			    Member() {
			    }
			  }
			  public B() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		NONE,
		// test name
		"<complete inside array type>"
	);
}
/*
 * Complete behind invalid array type
 */
public void test16() {
	runTestCheckDietParse(
		"""
			public class B {		\t
				class Member {}		\t
									\t
				int[				\t
				Obje				\t
			""",
		// completeBehind:
		"Obje",
		// expectedCompletionNodeToString:
		"<CompleteOnType:Obje>",
		"""
			public class B {
			  class Member {
			    Member() {
			    }
			  }
			  <CompleteOnType:Obje>;
			  public B() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"Obje",
		// expectedReplacedSource:
		"Obje",
		// test name
		"<complete behind invalid array type>"
	);
}
/*
 * Complete behind invalid base type
 */
public void test17() {
	this.runTestCheckMethodParse(
		"""
			class D {			\t
				class Member {}	\t
								\t
				void test() {	\t
					int.		\t
					test();		\t
				}				\t
			""",
		// completeBehind:
		"int.",
		// expectedCompletionNodeToString:
		"<CompleteOnClassLiteralAccess:int.>",
		"""
			class D {
			  class Member {
			    Member() {
			    }
			  }
			  D() {
			  }
			  void test() {
			    <CompleteOnClassLiteralAccess:int.>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"int.",
		// test name
		"<complete behind invalid base type>"
	);
}
/*
 * Complete behind incomplete local method header
 */
public void test18() {
	this.runTestCheckMethodParse(
		"""
			class E {				\t
				int bar() {			\t
					class Local {	\t
						int hello() {\t
			""",
		// completeBehind:
		"hello()",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class E {
			  E() {
			  }
			  int bar() {
			    class Local {
			      Local() {
			      }
			      int hello() {
			      }
			    }
			    <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete behind incomplete local method header>"
	);
}
/*
 * Complete behind catch variable
 */
public void test19() {
	this.runTestCheckMethodParse(
		"""
			public class Test {				\t
				void foo() {					\t
					try {						\t
					} catch (Exception e) {		\t
					}							\t
					e							\t
				}								\t
			}									\t
			""",
		// completeBehind:
		"\n\t\te",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e>",
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    <CompleteOnName:e>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"e",
		// expectedReplacedSource:
		"e",
		// test name
		"<complete behind catch variable>"
	);
}
/*
 * Complete on catch variable
 */
public void test20() {
	this.runTestCheckMethodParse(
		"""
			public class Test {				\t
				void foo() {					\t
					try {						\t
					} catch (Exception e) {		\t
						e						\t
					}							\t
				}								\t
			}									\t
			""",
		// completeBehind:
		"\n\t\t\te",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e>",
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    {
			      Exception e;
			      <CompleteOnName:e>;
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"e",
		// expectedReplacedSource:
		"e",
		// test name
		"<complete on catch variable>"
	);
}
/*
 * Complete on catch variable after syntax error
 */
public void test21() {
	this.runTestCheckMethodParse(
		"""
			public class Test {				\t
				void foo() {					\t
					try {						\t
						bar					\t
					} catch (Exception e) {		\t
						e						\t
					}							\t
				}								\t
			}									\t
			""",
		// completeBehind:
		"\n\t\t\te",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e>",
		"""
			public class Test {
			  public Test() {
			  }
			  void foo() {
			    {
			      Exception e;
			      <CompleteOnName:e>;
			    }
			  }
			}
			"""
		,
		// expectedCompletionIdentifier:
		"e",
		// expectedReplacedSource:
		"e",
		// test name
		"<complete on catch variable after syntax error>"
	);
}
/*
 * Complete on constructor type name
 * 1G1HF7P: ITPCOM:WIN98 - CodeAssist may not work in constructor signature
 */
public void test22() {
	runTestCheckDietParse(
		"""
			public class SomeType {
				public SomeType(int i){}
			}
			
			class SomeOtherType extends SomeType {
				SomeOtherType(int i){
					super(i);
				}
			}
			""",
		// completeBehind:
		"	SomeOther",
		// expectedCompletionNodeToString:
		"<CompleteOnType:SomeOther>",
		"""
			public class SomeType {
			  public SomeType(int i) {
			  }
			}
			class SomeOtherType extends SomeType {
			  <CompleteOnType:SomeOther>;
			  int i;
			  {
			  }
			  SomeOtherType() {
			  }
			}
			"""
		,
		// expectedCompletionIdentifier:
		"SomeOther",
		// expectedReplacedSource:
		"SomeOtherType",
		// test name
		"<complete on constructor type name>"
	);
}
/**
 * Complete in initializer in recovery mode
 */
public void test23() {
	this.runTestCheckMethodParse(
		"""
			package p;
			public class X {
			  void foo(){)
			    {
			      Obj
			    }
			  }
			}
			""",
		// completeBehind:
		"Obj",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Obj>",
		"""
			package p;
			public class X {
			  public X() {
			  }
			  void foo() {
			    {
			      <CompleteOnName:Obj>;
			    }
			  }
			}
			"""
		,
		// expectedCompletionIdentifier:
		"Obj",
		// expectedReplacedSource:
		"Obj",
		// test name
		"<complete in initializer>"
	);
}
/**
 * Complete after initializer in recovery mode
 */
public void test24() {
	this.runTestCheckMethodParse(
		"""
			package p;
			public class X {
			  void foo(){)
			    int v1;
			    {
			      int v2
			    }
			    Obj\
			  }
			}
			""",
		// completeBehind:
		"Obj",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Obj>",
		"""
			package p;
			public class X {
			  public X() {
			  }
			  void foo() {
			    int v1;
			    <CompleteOnName:Obj>;
			  }
			}
			"""
		,
		// expectedCompletionIdentifier:
		"Obj",
		// expectedReplacedSource:
		"Obj",
		// test name
		"<complete after initializer>"
	);
}
/**
 * Complete after dot, before a number {@code .<|>12}
 */
public void test25() {
	this.runTestCheckMethodParse(
		"""
			package p;
			public class X {
			  void foo(){
			      this.12
			  }
			}
			""",
		// completeBehind:
		"this.",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:this.>",
		"""
			package p;
			public class X {
			  public X() {
			  }
			  void foo() {
			    <CompleteOnMemberAccess:this.>;
			  }
			}
			"""
		,
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"this.",
		// test name
		"<complete after dot number>"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=201762
public void test26() {
	this.runTestCheckMethodParse(
		"""
			import org.eclipse.swt.*;
			import org.eclipse.swt.events.*;
			import org.eclipse.swt.widgets.*;
			
			public class Try {
			
			    void main(Shell shell) {
			
			        final Label label= new Label(shell, SWT.WRAP);
			        label.addPaintListener(new PaintListener() {
			            public void paintControl(PaintEvent e) {
			                e.gc.setLineCap(SWT.CAP_); // content assist after CAP_
			            }
			        });
			
			        shell.addControlListener(new ControlAdapter() { });
			
			        while (!shell.isDisposed()) { }
			    }
			}
			
			""",
		// completeBehind:
		"SWT.CAP_",
		// expectedCompletionNodeToString:
		"<CompleteOnName:SWT.CAP_>",
		"""
			import org.eclipse.swt.*;
			import org.eclipse.swt.events.*;
			import org.eclipse.swt.widgets.*;
			public class Try {
			  public Try() {
			  }
			  void main(Shell shell) {
			    final Label label;
			    label.addPaintListener(new PaintListener() {
			  public void paintControl(PaintEvent e) {
			    e.gc.setLineCap(<CompleteOnName:SWT.CAP_>);
			  }
			});
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"CAP_",
		// expectedReplacedSource:
		"SWT.CAP_",
		// test name
		"<complete after dot number>"
	);
}
}
