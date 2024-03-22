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

/**
 * Completion is expected to be a name reference.
 */
public class NameReferenceCompletionTest extends AbstractCompletionTest {
public NameReferenceCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(NameReferenceCompletionTest.class);
}
/*
 * Regression test for 1FTZ849.
 * The instance creation before the completion is not properly closed, and thus
 * the completion parser used to think the completion was on a type.
 */
public void test1FTZ849() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					X o = new X;					\t
					fred.xyz;						\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred.x",
		// expectedCompletionNodeToString:
		"<CompleteOnName:fred.x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    X o;
			    <CompleteOnName:fred.x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"fred.xyz",
		// test name
		"<1FTZ849>"
	);
}
/*
 * Completion in a field initializer with no syntax error.
 */
public void test1FUUP73() {
	runTestCheckDietParse(
		"""
			public class A {				\t
				String s = "hello";		\t
				Object o = s.concat("boo");\t
			""",
		// completeBehind:
		"Object o = s",
		// expectedCompletionNodeToString:
		"<CompleteOnName:s>",
		"""
			public class A {
			  String s;
			  Object o = <CompleteOnName:s>;
			  public A() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"s",
		// expectedReplacedSource:
		"s",
		// test name
		"<1FUUP73>"
	);
	runTestCheckDietParse(
		"""
			public class A {				\t
				String s = "hello";		\t
				Object o = s.concat("boo");\t
			""",
		// completeBehind:
		"Object o = s.c",
		// expectedCompletionNodeToString:
		"<CompleteOnName:s.c>",
		"""
			public class A {
			  String s;
			  Object o = <CompleteOnName:s.c>;
			  public A() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"c",
		// expectedReplacedSource:
		"s.concat",
		// test name
		"<1FUUP73>"
	);
}
/*
 * Regression test for 1FVRQQA.
 */
public void test1FVRQQA_1() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					Enumeration e = null; 			\t
					e.to							\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"e.to",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e.to>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    Enumeration e;
			    <CompleteOnName:e.to>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"to",
		// expectedReplacedSource:
		"e.to",
		// test name
		"<1FVRQQA_1>"
	);
}
/*
 * Regression test for 1FVRQQA.
 */
public void test1FVRQQA_2() {
	this.runTestCheckMethodParse(
		"""
			class X {												\t
				void foo() {										\t
					for (Enumeration e = getSomeEnumeration(); e.has\t
				}													\t
			}														\t
			""",
		// completeBehind:
		"e.has",
		// expectedCompletionNodeToString:
		"<CompleteOnName:e.has>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    Enumeration e;
			    for (; <CompleteOnName:e.has>; )\s
			      ;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"has",
		// expectedReplacedSource:
		"e.has",
		// test name
		"<1FVRQQA_2>"
	);
}
/*
 * Regression test for 1FVT66Q.
 */
public void test1FVT66Q_1() {
	this.runTestCheckMethodParse(
		"""
			package test;						\t
												\t
			public class Test {				\t
				public void foo() {				\t
					final int codeAssistTarget= 3;\t
												\t
					Thread t= new Thread() {	\t
						public void run() {		\t
							codeAss				\t
						}						\t
					};							\t
					codeA						\t
				}								\t
			}									\t
			""",
		// completeBehind:
		"	codeAss",
		// expectedCompletionNodeToString:
		"<CompleteOnName:codeAss>",
		"""
			package test;
			public class Test {
			  public Test() {
			  }
			  public void foo() {
			    final int codeAssistTarget;
			    Thread t;
			    new Thread() {
			      public void run() {
			        <CompleteOnName:codeAss>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"codeAss",
		// expectedReplacedSource:
		"codeAss",
		// test name
		"<1FVT66Q_1>"
	);
}
/*
 * Regression test for 1FVT66Q.
 */
public void test1FVT66Q_2() {
	this.runTestCheckMethodParse(
		"""
			package test;						\t
												\t
			public class Test {				\t
				public void foo() {				\t
					final int codeAssistTarget= 3;\t
												\t
					Thread t= new Thread() {	\t
						public void run() {		\t
							codeAss				\t
						}						\t
					};							\t
					codeA						\t
				}								\t
			}									\t
			""",
		// completeBehind:
		"\n		codeA",
		// expectedCompletionNodeToString:
		"<CompleteOnName:codeA>",
		"""
			package test;
			public class Test {
			  public Test() {
			  }
			  public void foo() {
			    final int codeAssistTarget;
			    Thread t;
			    <CompleteOnName:codeA>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"codeA",
		// expectedReplacedSource:
		"codeA",
		// test name
		"<1FVT66Q_2>"
	);
}
/*
 * Regression test for 1G8DE30.
 */
public void test1G8DE30() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					new Runnable() {				\t
						public void run() {			\t
							Bar						\t
						}							\t
					};								\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"public void run() {				\n				",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    new Runnable() {
			      public void run() {
			        <CompleteOnName:>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Bar",
		// test name
		"<1G8DE30>"
	);
}
/*
 * Completion on an empty name reference.
 */
public void testEmptyNameReference() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"int i = 0;							\n		",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference>"
	);
}
/*
 * Completion on an empty name reference after a cast.
 */
public void testEmptyNameReferenceAfterCast() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					X x = (X)						\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"(X)",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    X x = (X) <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference after cast>"
	);
}
/*
 * Completion on an empty name reference after + operator.
 */
public void testEmptyNameReferenceAfterPlus() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					1 + 							\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"1 +",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference after + operator>"
	);
}
/*
 * Completion on an empty name reference in an array dimension.
 */
public void testEmptyNameReferenceInArrayDim() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					int[]							\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"int[",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference in array dim>"
	);
}
/*
 * Completion on an empty name reference in inner class.
 */
public void testEmptyNameReferenceInInnerClass() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					class Y {						\t
						void bar() {				\t
													\t
						}							\t
					}								\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"\n				",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    class Y {
			      Y() {
			      }
			      void bar() {
			        <CompleteOnName:>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference in inner class>"
	);
}
/*
 * Completion in the statement following an if expression.
 */
public void testInIfThenStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					if (bar()) 						\t
													\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"\n			",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete in if then statement>"
	);
}
/*
 * Completion in the statement following an if expression.
 */
public void testInIfThenWithInstanceOfStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					if (this instanceof Bar) 		\t
													\t
													\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"\n			",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    if ((this instanceof Bar))
			        <CompleteOnName:>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete in if then statement>"
	);
}
/*
 * Completion on a name reference inside an inner class in a field initializer.
 */
public void testInnerClassFieldInitializer() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				Object o = new Object() {			\t
					void foo() {					\t
						xyz							\t
					}								\t
				};									\t
			}										\t
			""",
		// completeBehind:
		"xyz",
		// expectedCompletionNodeToString:
		"<CompleteOnName:xyz>",
		"""
			class Bar {
			  Object o = new Object() {
			    void foo() {
			      <CompleteOnName:xyz>;
			    }
			  };
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"xyz",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on name reference in inner class in field initializer>"
	);
}
/*
 * Completion on an empty name reference inside an invocation in a field initializer.
 */
public void testInvocationFieldInitializer() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				String s = fred(1 + );				\t
				void foo() {						\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"(1 + ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  String s = (1 + <CompleteOnName:>);
			  Bar() {
			  }
			  void foo() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<complete on empty name reference in invocation in field initializer>"
	);
}
/*
 * Completion inside an anonymous inner class which is
 * inside a method invocation with receiver.
 */
public void testMethodInvocationAnonymousInnerClass() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					primary().bizz(						\t
						new X() {						\t
							void fuzz() {				\t
								x.y.z					\t
							}							\t
						}								\t
					);									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:x.>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    new X() {
			      void fuzz() {
			        <CompleteOnName:x.>;
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"x.y.z",
		// test name
		"<complete inside anonymous inner class inside method invocation 1>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is in the
 * first type reference.
 */
public void testQualifiedNameReferenceShrinkAll() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					a.b.c.Xxx o = new Y(i);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"		a",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnName:a>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"a",
		// expectedReplacedSource:
		"a",
		// test name
		"<complete on qualified name reference (shrink all)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the first dot.
 */
public void testQualifiedNameReferenceShrinkAllButOne() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.x.x.super();				\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"Bar.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Bar.>",
		"""
			class Bar {
			  public class InnerBar {
			    public InnerBar() {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      super();
			      <CompleteOnName:Bar.>;
			    }
			  }
			  static Bar x;
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Bar.",
		// test name
		"<complete on qualified name reference (shrink all but one)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the first dot.
 */
public void testQualifiedNameReferenceShrinkAllButOne2() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					a.b.c.X o = new Y(i);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"		a.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a.>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnName:a.>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.",
		// test name
		"<complete on qualified name reference (shrink all but one) 2>"
	);
}
/*
 * Completion on a qualified name reference,where the cursor is right after the end
 * of the last name reference.
 */
public void testQualifiedNameReferenceShrinkNone() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.x.x.super();				\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"Bar.x.x",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Bar.x.x>",
		"""
			class Bar {
			  public class InnerBar {
			    public InnerBar() {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      super();
			      <CompleteOnName:Bar.x.x>;
			    }
			  }
			  static Bar x;
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"Bar.x.x",
		// test name
		"<complete on qualified name reference (shrink none)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the end
 * of the last type reference.
 */
public void testQualifiedNameReferenceShrinkNone2() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					a.b.c.Xxx o = new Y(i);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a.b.c.X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnName:a.b.c.X>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"a.b.c.Xxx",
		// test name
		"<complete on qualified name reference (shrink none) 2>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the
 * last dot.
 */
public void testQualifiedNameReferenceShrinkOne() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.x.x.super();				\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"Bar.x.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:Bar.x.>",
		"""
			class Bar {
			  public class InnerBar {
			    public InnerBar() {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      super();
			      <CompleteOnName:Bar.x.>;
			    }
			  }
			  static Bar x;
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"Bar.x.",
		// test name
		"<complete on qualified name reference (shrink one)>"
	);
}
/*
 * Completion on a qualified name reference, where the cursor is right after the
 * last dot.
 */
public void testQualifiedNameReferenceShrinkOne2() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					a.b.c.X o = new Y(i);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"a.b.c.",
		// expectedCompletionNodeToString:
		"<CompleteOnName:a.b.c.>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnName:a.b.c.>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.b.c.",
		// test name
		"<complete on qualified name reference (shrink one) 2>"
	);
}
/*
 * Completion on a qualified name reference that contains a unicode.
 */
public void testUnicode() {
	this.runTestCheckMethodParse(
		"""
			class X {				\t
				void foo() {		\t
					bar.\\u005ax 	\t
				}					\t
			}						\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnName:bar.Zx>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    <CompleteOnName:bar.Zx>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"Zx",
		// expectedReplacedSource:
		"bar.\\u005ax",
		// test name
		"<complete on unicode>"
	);
}
}
