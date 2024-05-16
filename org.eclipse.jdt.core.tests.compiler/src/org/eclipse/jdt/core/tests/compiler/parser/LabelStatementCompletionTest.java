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
 * Completion is expected to be in a LabeledStatement.
 */
public class LabelStatementCompletionTest extends AbstractCompletionTest {
public LabelStatementCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(LabelStatementCompletionTest.class);
}
/*
 * Completion inside an inner class defined inside a labeled statement.
 */
public void test1FTEO9L() {
	String cu =
		"""
		package p; 				\t
									\t
		class CCHelper {			\t
			class Member1 {			\t
			}						\t
			class Member2 {			\t
			}						\t
			void foo() {			\t
			}						\t
		}							\t
									\t
		public class CC {			\t
			void foo() {			\t
				new CCHelper()		\t
					.new CCHelper()	\t
					.new M			\t
			}						\t
		}							\t
		""";
	// first case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.n",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:new CCHelper().n>",
		"""
			package p;
			class CCHelper {
			  class Member1 {
			    Member1() {
			    }
			  }
			  class Member2 {
			    Member2() {
			    }
			  }
			  CCHelper() {
			  }
			  void foo() {
			  }
			}
			public class CC {
			  public CC() {
			  }
			  void foo() {
			    <CompleteOnMemberAccess:new CCHelper().n>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"n",
		// expectedReplacedSource:
		"new",
		// test name
		"<regression test 1FTEO9L (first case)>"
	);
	// second case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.new CC",
		// expectedCompletionNodeToString:
		"<CompleteOnType:CC>",
		"""
			package p;
			class CCHelper {
			  class Member1 {
			    Member1() {
			    }
			  }
			  class Member2 {
			    Member2() {
			    }
			  }
			  CCHelper() {
			  }
			  void foo() {
			  }
			}
			public class CC {
			  public CC() {
			  }
			  void foo() {
			    new CCHelper().new <CompleteOnType:CC>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"CC",
		// expectedReplacedSource:
		"CCHelper",
		// test name
		"<regression test 1FTEO9L (second case)>"
	);
	// third case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.new CCHelper()		\n" +
		"			.n",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:new CCHelper().new CCHelper().n>",
		"""
			package p;
			class CCHelper {
			  class Member1 {
			    Member1() {
			    }
			  }
			  class Member2 {
			    Member2() {
			    }
			  }
			  CCHelper() {
			  }
			  void foo() {
			  }
			}
			public class CC {
			  public CC() {
			  }
			  void foo() {
			    <CompleteOnMemberAccess:new CCHelper().new CCHelper().n>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"n",
		// expectedReplacedSource:
		"new",
		// test name
		"<regression test 1FTEO9L (third case)>"
	);
	// fourth case
	this.runTestCheckMethodParse(
		// compilationUnit:
		cu,
		// completeBehind:
		"			.new CCHelper()		\n" +
		"			.new M",
		// expectedCompletionNodeToString:
		"<CompleteOnType:M>",
		"""
			package p;
			class CCHelper {
			  class Member1 {
			    Member1() {
			    }
			  }
			  class Member2 {
			    Member2() {
			    }
			  }
			  CCHelper() {
			  }
			  void foo() {
			  }
			}
			public class CC {
			  public CC() {
			  }
			  void foo() {
			    new CCHelper().new CCHelper().new <CompleteOnType:M>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"M",
		// expectedReplacedSource:
		"M",
		// test name
		"<regression test 1FTEO9L (fourth case)>"
	);
}
/*
 * Completion inside a case that has an identifier as its constant expression.
 */
public void testInCaseWithIdentifier() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					label1: {								\t
						switch (i) {						\t
							case a: label2: X o = new Object();\t
						}									\t
					}										\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    {
			      {
			        <CompleteOnName:X>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1", "label2"},
		// test name
		"<complete in case with identifier>"
	);
}
/*
 * Completion inside a case that has a number as its constant expression.
 */
public void testInCaseWithNumberConstant() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					label1: {								\t
						switch (i) {						\t
							case 1: label2: X o = new Object();\t
						}									\t
					}										\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    {
			      {
			        <CompleteOnName:X>;
			      }
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1", "label2"},
		// test name
		"<complete in case with number>"
	);
}
/*
 * Completion inside an inner class defined inside a labeled statement.
 */
public void testInLabeledInnerClass() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					label1: {							\t
						Object o = new Object() {		\t
							void fred() {				\t
								label2: {				\t
									X o = new Object();	\t
								}						\t
							}							\t
						};								\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    {
			      Object o = new Object() {
			        void fred() {
			          label2: {
			  <CompleteOnType:X> o;
			}
			        }
			      };
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label2"},
		// test name
		"<complete in labeled inner class>"
	);
}
/*
 * Completion inside an inner class defined inside a labeled statement with a syntax error
 * just before the labeled statement.
 */
public void testInLabeledInnerClassWithErrorBefore() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					int i == 2;\s
					label1: {							\t
						Object o = new Object() {		\t
							void fred() {				\t
								label2: {				\t
									X o = new Object();	\t
								}						\t
							}							\t
						};								\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    {
			      Object o;
			      new Object() {
			        void fred() {
			          {
			            <CompleteOnName:X>;
			          }
			        }
			      };
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label2"},
		// test name
		"<complete in labeled inner class with syntax error before>"
	);
}
/*
 * Completion inside a labeled statement one level deep.
 */
public void testOneLevelDeep() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					label1: X o = new Object();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnName:X>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1"},
		// test name
		"<complete in one level deep>"
	);
}
/*
 * Completion inside a labeled statement which is the second one in the method.
 */
public void testSecondLabel() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					label1: buzz();						\t
					label2: X o = new Object();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnName:X>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label2"},
		// test name
		"<complete in second labeled statement>"
	);
}
/*
 * Completion inside a labeled statement two level deep.
 */
public void testTwoLevelDeep() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					label1: {							\t
						label2: X o = new Object();		\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    {
			      <CompleteOnName:X>;
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"X",
		// expectedLabels:
		new String[] {"label1", "label2"},
		// test name
		"<complete in two level deep>"
	);
}
}
