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
 * Completion is expected to be a MethodInvocation.
 */
public class MethodInvocationCompletionTest extends AbstractCompletionTest {
public MethodInvocationCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(MethodInvocationCompletionTest.class);
}
/*
 * Completion with no receiver inside a for statement.
 */
public void test1FVVWS8_1() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					for (int i = 10; i > 0; --i)	\t
						fred(						\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnMessageSend:fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FVVWS8_1>"
	);
}
/*
 * Completion with no receiver inside an if statement.
 */
public void test1FVVWS8_2() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					if (true)						\t
						fred(						\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FVVWS8_2>"
	);
}
/*
 * Completion with no receiver inside a for statement
 * and after a field access.
 */
public void test1FW2ZTB_1() {
	this.runTestCheckMethodParse(
		"""
			class X {										\t
				int[] array;								\t
				void foo() {								\t
					for (int i = this.array.length; i > 0; --i)\t
						fred(								\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		"""
			class X {
			  int[] array;
			  X() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnMessageSend:fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FW2ZTB_1"
	);
}
/*
 * Completion with no receiver inside another message send
 * and after a field access in a previous argument.
 */
public void test1FW2ZTB_2() {
	this.runTestCheckMethodParse(
		"""
			class X {										\t
				int[] array;								\t
				void foo() {								\t
					bar(this.array.length, 10, fred(		\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		"""
			class X {
			  int[] array;
			  X() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FW2ZTB_2"
	);
}
/*
 * Complete on method invocation with expression receiver
 * inside another invocation with no receiver.
 */
public void test1FW35YZ_1() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					bar(primary().fred(				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:primary().fred()>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:primary().fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<1FW35YZ_1>"
	);
}
/*
 * Complete on qualified allocation expression
 * inside an invocation with no receiver.
 */
public void test1FW35YZ_2() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					bar(primary().new X(			\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X(",
		// expectedCompletionNodeToString:
		"<CompleteOnQualifiedAllocationExpression:primary().new X()>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnQualifiedAllocationExpression:primary().new X()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<1FW35YZ_2>"
	);
}
/*
 * Completion with primary receiver.
 */
public void test1FWYBKF() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
						this.x.bar(					\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"bar(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:this.x.bar()>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:this.x.bar()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"bar(",
		// test name
		"<1FWYBKF>"
	);
}
/*
 * Completion just after a parameter which is a message send.
 */
public void test1GAJBUQ() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					x.y.Z.fred(buzz());				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(buzz()",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:x.y.Z.fred(buzz())>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:x.y.Z.fred(buzz())>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(buzz()",
		// test name
		"<1GAJBUQ>"
	);
}
/*
 * Completion just before the second parameter, the first parameter being an empty
 * anonymous class.
 */
public void testAfterEmptyAnonymous() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred(new Runnable() {}, 2, i);\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(new Runnable() {}, ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    this.fred(new Runnable() {
			}, <CompleteOnName:>, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before second parameter, the first parameter being an empty anonymous class>"
	);
}
/*
 * Completion just after the first parameter.
 */
public void testAfterFirstParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred("abc" , 2, i);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(\"abc\" ",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:this.fred(\"abc\")>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:this.fred("abc")>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(\"abc\" ",
		// test name
		"<completion just after first parameter>"
	);
}
/*
 * Completion just before the first parameter.
 */
public void testBeforeFirstParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred(1, 2, i);				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:this.fred(<CompleteOnName:>, 2, i)>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:this.fred(<CompleteOnName:>, 2, i)>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(1, 2, i)",
		// test name
		"<completion just before first parameter>"
	);
}
/*
 * Completion just before the last parameter.
 */
public void testBeforeLastParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred(1, 2, i);				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    this.fred(1, 2, <CompleteOnName:>, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before last parameter>"
	);
}
/*
 * Completion just before the second parameter.
 */
public void testBeforeSecondParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred(1, 2, i);				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(1, ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    this.fred(1, <CompleteOnName:>, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before second parameter>"
	);
}
/*
 * Completion on empty name inside the expression of the first parameter.
 */
public void testEmptyInFirstParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred("abc" + , 2, i);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(\"abc\" +",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    this.fred(("abc" + <CompleteOnName:>), 2, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion empty in first parameter>"
	);
}
/*
 * Completion inside the expression of the first parameter.
 */
public void testInFirstParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred("abc" + bizz, 2, i);\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(\"abc\" + bi",
		// expectedCompletionNodeToString:
		"<CompleteOnName:bi>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    this.fred(("abc" + <CompleteOnName:bi>), 2, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"bi",
		// expectedReplacedSource:
		"bizz",
		// test name
		"<completion inside first parameter>"
	);
}
/*
 * Completion inside an if statement.
 */
public void testInIfStatement() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					if (true) {						\t
						bar.fred();					\t
					}								\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:bar.fred()>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    if (true)
			        {
			          <CompleteOnMessageSend:bar.fred()>;
			        }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred()",
		// test name
		"<completion inside a if statement>"
	);
}
/*
 * Completion in labeled method invocation with expression receiver.
 */
public void testLabeledWithExpressionReceiver() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					label1: bar().fred(1, 2, o);	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    label1: bar().fred(1, 2, <CompleteOnName:>, o);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// expectedLabels:
		new String[] {"label1"},
		// test name
		"<completion in labeled method invocation with expression receiver>"
	);
}
/*
 * Completion in labeled method invocation without receiver.
 */
public void testLabeledWithoutReceiver() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					label1: fred(1, 2, o);			\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    label1: fred(1, 2, <CompleteOnName:>, o);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// expectedLabels:
		new String[] {"label1"},
		// test name
		"<completion in labeled method invocation without receiver>"
	);
}
/*
 * MethodInvocation ::= Name '(' ArgumentListopt ')'
 */
public void testNoReceiver() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					fred();							\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:fred()>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<completion on method invocation with no receiver>"
	);
}
/*
 * Completion just before the first parameter with a space after the open parenthesis.
 */
public void testSpaceThenFirstParameter() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					this.fred( 1, 2, i);			\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred( ",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    this.fred(<CompleteOnName:>, 2, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion just before first parameter with a space after open parenthesis>"
	);
}
/*
 * MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'
 */
public void testSuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					super.fred(1, 2, i);			\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:super.fred()>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:super.fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// test name
		"<completion on super method invocation>"
	);
}
/*
 * Complete on method invocation with expression receiver.
 */
public void testWithExpressionReceiver() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					bar().fred();					\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:bar().fred()>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:bar().fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred()",
		// test name
		"<completion on method invocation with expression receiver>"
	);
}
/*
 * Completion with a name receiver.
 */
public void testWithNameReceiver() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					Vector v = new Vector();		\t
					v.addElement("1");			\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"addElement(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:v.addElement()>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    Vector v;
			    <CompleteOnMessageSend:v.addElement()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"addElement(",
		// test name
		"<completion with name receiver>"
	);
}
/*
 * Completion with a name receiver after conditional expression.
 */
public void testWithNameReceiverAfterConditionalExpression() {
	this.runTestCheckMethodParse(
		"""
			class X {								\t
				void foo() {						\t
					buzz.test(cond ? max : min);	\t
					bar.fred();						\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"fred(",
		// expectedCompletionNodeToString:
		"<CompleteOnMessageSend:bar.fred()>",
		"""
			class X {
			  X() {
			  }
			  void foo() {
			    <CompleteOnMessageSend:bar.fred()>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"fred(",
		// expectedLabels:
		new String[] {},
		// test name
		"<completion with name receiver after conditional expression>"
	);
}
/*
 * Completion with a name receiver and 2 arguments.
 */
public void testWithNameReceiverAndTwoArgs() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					X x = new X();					\t
					x.fred(1, 2, o);				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"x.fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    X x;
			    x.fred(1, 2, <CompleteOnName:>, o);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion with name receiver and 2 arguments>"
	);
}
/*
 * Completion with a qualified name receiver.
 */
public void testWithQualifiedNameReceiver() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					X x = new X();					\t
					y.x.fred(1, 2, o);				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"x.fred(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    X x;
			    y.x.fred(1, 2, <CompleteOnName:>, o);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion with qualified name receiver>"
	);
}
}
