/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 * Completion is expected to be a ReferenceType.
 */
public class ReferenceTypeCompletionTest extends AbstractCompletionTest {
public ReferenceTypeCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ReferenceTypeCompletionTest.class);
}
/*
 * Regression test for 1FTZCIG.
 */
public void test1FTZCIG() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					new X() {						\t
						protected void bar() {		\t
						}							\t
					}								\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"p",
		// expectedCompletionNodeToString:
		"<CompleteOnType:p>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    new X() {
			      <CompleteOnType:p>;
			      void bar() {
			      }
			    };
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"p",
		// expectedReplacedSource:
		"protected",
		// test name
		"<1FTZCIG>"
	);
}
/*
 * Block ::= OpenBlock '{' <BlockStatementsopt> '}'
 */
public void testBlock() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					try {							\t
						Xxx o = new Y();			\t
					} catch (Exception e) {			\t
					}								\t
				}									\t
			}										\t
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
		"Xxx",
		// test name
		"<complete on block>"
	);
}
/*
 * BlockStatements ::= BlockStatements <BlockStatement>
 */
public void testBlockStatements() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					Xxx o = new Y();				\t
				}									\t
			}										\t
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
			    <CompleteOnName:X>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on block statements>"
	);
}
/*
 * CatchClause ::= 'catch' '(' <FormalParameter> ')' Block
 */
public void testCatchClause1() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					try {							\t
						fred();						\t
					} catch (Xxx e) {				\t
					}								\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    try
			      {
			        fred();
			      }
			    catch (<CompleteOnException:X>  )
			      {
			      }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on catch clause 1>"
	);
}
/*
 * CatchClause ::= 'catch' '(' <FormalParameter> ')' Block
 */
public void testCatchClause2() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					try {							\t
						fred();						\t
					} catch (final Xxx e) {			\t
					}								\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    try
			      {
			        fred();
			      }
			    catch (<CompleteOnException:X>  )
			      {
			      }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on catch clause 2>"
	);
}
/*
 * CatchClause ::= 'catch' '(' <FormalParameter> ')' Block
 */
public void testCatchClause3() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					try {							\t
						fred();						\t
					} catch (x.y.Xxx e) {			\t
					}								\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"x.y.X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:x.y.X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    try
			      {
			        fred();
			      }
			    catch (<CompleteOnException:x.y.X>  )
			      {
			      }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"x.y.Xxx",
		// test name
		"<complete on catch clause 3>"
	);
}
/*
 * ClassBody ::= '{' <ClassBodyDeclarationsopt> '}'
 */
public void testClassBody() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				Xxx foo() {							\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  <CompleteOnType:X>
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on  class body>"
	);
}
/*
 * ClassBodyDeclarations ::= ClassBodyDeclarations <ClassBodyDeclaration>
 */
public void testClassBodyDeclarations() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				int i = 0;							\t
				Xxx foo() {							\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  int i;
			  Bar() {
			  }
			  <CompleteOnType:X>
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on  class body declarations>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression1() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					new Xxx().zzz();				\t
				}									\t
			}
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
			    new <CompleteOnType:X>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 1>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression2() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					new Y(new Xxx()).zzz();			\t
				}									\t
			}
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
			    new Y(new <CompleteOnType:X>());
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 2>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression3() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					new Y(1, true, new Xxx()).zzz();\t
				}									\t
			}
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
			    new Y(1, true, new <CompleteOnType:X>()).zzz();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 3>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' <ClassType> '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression4() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					fred().new Y(new Xxx()).zzz();	\t
				}									\t
			}
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
			    fred().new Y(new <CompleteOnType:X>()).zzz();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression 4>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName1() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				static Bar baz;							\t
				public class X {						\t
					void foo() {						\t
						Bar.baz.new Xxx();				\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      Bar.baz.new <CompleteOnType:X>();
			    }
			  }
			  static Bar baz;
			  <clinit>() {
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 1>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName2() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				static Bar baz;							\t
				public class X {						\t
					void foo() {						\t
						new Y(Bar.baz.new Xxx());		\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      new Y(Bar.baz.new <CompleteOnType:X>());
			    }
			  }
			  static Bar baz;
			  <clinit>() {
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 2>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName3() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				static Bar baz;							\t
				public class X {						\t
					void foo() {						\t
						new Y(1, true, Bar.baz.new Xxx());\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      new Y(1, true, Bar.baz.new <CompleteOnType:X>());
			    }
			  }
			  static Bar baz;
			  <clinit>() {
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 3>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName4() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				static Bar baz;							\t
				public class X {						\t
					void foo() {						\t
						fred().new Y(Bar.baz.new Xxx());	\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      fred().new Y(Bar.baz.new <CompleteOnType:X>());
			    }
			  }
			  static Bar baz;
			  <clinit>() {
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with name 4>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary1() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				public class X {						\t
					void foo() {						\t
						new Bar().new Xxx();			\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      new Bar().new <CompleteOnType:X>();
			    }
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 1>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary2() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				public class X {						\t
					void foo() {						\t
						new Y(new Bar().new Xxx());		\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      new Y(new Bar().new <CompleteOnType:X>());
			    }
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 2>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary3() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				public class X {						\t
					void foo() {						\t
						fred().new Y(new Bar().new Xxx());\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      fred().new Y(new Bar().new <CompleteOnType:X>());
			    }
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 3>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary4() {
	this.runTestCheckMethodParse(
		"""
			public class Bar {							\t
				public class X {						\t
					void foo() {						\t
						new Y(1, true, new Bar().new Xxx());
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"new X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			public class Bar {
			  public class X {
			    public X() {
			    }
			    void foo() {
			      new Y(1, true, new Bar().new <CompleteOnType:X>());
			    }
			  }
			  public Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class instance creation expression with primary 4>"
	);
}
/*
 * ClassTypeList ::= ClassTypeList ',' <ClassTypeElt>
 */
public void testClassTypeList() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				void foo() throws Exception, Xxx {	\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() throws Exception, <CompleteOnException:X> {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on class type list>"
	);
}
/*
 * ConstructorBody ::= NestedMethod '{' <BlockStatementsopt> '}'
 */
public void testConstructorBody() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				Bar() {								\t
					Xxx o = new Y();				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnName:X>",
		"""
			class Bar {
			  Bar() {
			    super();
			    <CompleteOnName:X>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on constructor body>"
	);
}
/*
 * ConstructorDeclarator ::= 'Identifier' '(' <FormalParameterListopt> ')'
 */
public void testConstructorDeclarator() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				Bar(Xxx o) {						\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar(<CompleteOnType:X> o) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on constructor declarator>"
	);
}
/*
 * The reference type is burried in several blocks
 */
public void testDeepReference() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					if (a == 2) {					\t
					}								\t
					try {							\t
					} finally {						\t
						if (1 == fgh) {				\t
							Xxx o = null;			\t
						}							\t
					}								\t
				}									\t
			}										\t
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
			    if ((a == 2))
			        {
			        }
			    try
			      {
			      }
			    finally
			      {
			        if ((1 == fgh))
			            {
			              <CompleteOnType:X> o;
			            }
			      }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on deep type>"
	);
}
/*
 * Super ::= 'extends' <ClassType>
 */
public void testExtendsClass() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar extends Xxx {					\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnClass:X>",
		"""
			class Bar extends <CompleteOnClass:X> {
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on extends>"
	);
}
/*
 * ExtendsInterfaces ::= 'extends' <InterfaceTypeList>
 */
public void testExtendsInterface() {
	runTestCheckDietParse(
		// compilationUnit:
		"interface Bar extends Xxx {				\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnInterface:X>",
		// expectedUnitDisplayString:
		"interface Bar extends <CompleteOnInterface:X> {\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on extends>"
	);
}
/*
 * FieldDeclaration ::= Modifiersopt <Type> VariableDeclarators ';'
 * where Modifiersopt is not empty
 */
public void testFieldDeclarationWithModifiers() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				public final Xxx foo;				\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>;",
		"""
			class Bar {
			  <CompleteOnType:X>;
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on field declaration with modifiers>"
	);
}
/*
 * FieldDeclaration ::= Modifiersopt <Type> VariableDeclarators ';'
 * where Modifiersopt is empty
 */
public void testFieldDeclarationWithoutModifiers() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				Xxx foo;							\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>;",
		"""
			class Bar {
			  <CompleteOnType:X>;
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on field declaration without modifiers>"
	);
}
/*
 * FormalParameter ::= Modifiers <Type> VariableDeclaratorId
 */
public void testFormalParameter() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				void foo(final Xxx x) {				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo(final <CompleteOnType:X> x) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on formal parameter>"
	);
}
/*
 * FormalParameterList ::= FormalParameterList ',' <FormalParameter>
 */
public void testFormalParameterList() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				void foo(int i, final Object o, Xxx x) {
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo(int i, final Object o, <CompleteOnType:X> x) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on formal parameter list>"
	);
}
/*
 * ForStatement ::= 'for' '(' <ForInitopt> ';' Expressionopt ';' ForUpdateopt ')' Statement
 * or
 * ForStatementNoShortIf ::= 'for' '(' <ForInitopt> ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf
 */
public void testForStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					for (Xxx o = new Y(); o.size() < 10; ) {
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
			    <CompleteOnName:X>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on for statement>"
	);
}
/*
 * Interfaces ::= 'implements' <InterfaceTypeList>
 */
public void testImplements() {
	runTestCheckDietParse(
		// compilationUnit:
		"class Bar implements Xxx {					\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnInterface:X>",
		"""
			class Bar implements <CompleteOnInterface:X> {
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on implements>"
	);
}
/*
 * RelationalExpression ::= RelationalExpression 'instanceof' <ReferenceType>
 */
public void testInstanceOf() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				boolean foo() {							\t
					return this instanceof Xxx;			\t
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
			  boolean foo() {
			    return (this instanceof <CompleteOnType:X>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on instanceof>"
	);
}
/*
 * InterfaceBody ::= '{' <InterfaceMemberDeclarationsopt> '}'
 */
public void testInterfaceBody() {
	runTestCheckDietParse(
		"""
			interface Bar {						\t
				Xxx foo();							\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			interface Bar {
			  <CompleteOnType:X>
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on interface body>"
	);
}
/*
 * InterfaceMemberDeclarations ::= InterfaceMemberDeclarations <InterfaceMemberDeclaration>
 */
public void testInterfaceMemberDeclarations() {
	runTestCheckDietParse(
		"""
			interface Bar {						\t
				int CONSTANT = 0;					\t
				Xxx foo();							\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			interface Bar {
			  int CONSTANT;
			  <clinit>() {
			  }
			  <CompleteOnType:X>
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on interface member declarations>"
	);
}
/*
 * InterfaceTypeList ::= InterfaceTypeList ',' <InterfaceType>
 */
public void testInterfaceTypeList() {
	runTestCheckDietParse(
		// compilationUnit:
		"interface Bar extends Comparable, Xxx {	\n" +
		"}											\n",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnInterface:X>",
		// expectedUnitDisplayString:
		"interface Bar extends Comparable, <CompleteOnInterface:X> {\n" +
		"}\n",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on interface list>"
	);
}
/*
 * LocalVariableDeclaration ::= Modifiers <Type> VariableDeclarators
 */
public void testLocalVariableDeclaration() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					final Xxx o = new Y();				\t
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
		"Xxx",
		// test name
		"<complete on local variable declaration>"
	);
}
/*
 * MethodBody ::= NestedMethod '{' <BlockStatementsopt> '}'
 */
public void testMethodBody() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					Xxx o = new Y();				\t
				}									\t
			}										\t
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
		"Xxx",
		// test name
		"<complete on method body>"
	);
}
/*
 * MethodDeclarator ::= 'Identifier' '(' <FormalParameterListopt> ')' Dimsopt
 */
public void testMethodDeclarator() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				void foo(Xxx o) {					\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo(<CompleteOnType:X> o) {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method declarator>"
	);
}
/*
 * MethodHeader ::= Modifiersopt <Type> MethodDeclarator Throwsopt
 * where Modifiersopt is not empty
 */
public void testMethodHeaderWithModifiers() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				public static Xxx foo() {			\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  <CompleteOnType:X>
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method header with modifiers>"
	);
}
/*
 * MethodHeader ::= Modifiersopt <Type> MethodDeclarator Throwsopt
 * where Modifiersopt is empty
 */
public void testMethodHeaderWithoutModifiers() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				Xxx foo() {							\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  <CompleteOnType:X>
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on method header without modifiers>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is in the
 * first type reference.
 */
public void testQualifiedTypeReferenceShrinkAll() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					new a.b.c.Xxx();		\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"		new a",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    new <CompleteOnType:a>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"a",
		// expectedReplacedSource:
		"a",
		// test name
		"<complete on qualified type reference (shrink all)>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is right after the first dot.
 */
public void testQualifiedTypeReferenceShrinkAllButOne() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					new a.b.c.Xxx();		\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"a.",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a.>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    new <CompleteOnType:a.>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.",
		// test name
		"<complete on qualified type reference (shrink all but one)>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is right after the end
 * of the last type reference.
 */
public void testQualifiedTypeReferenceShrinkNone() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					new a.b.c.Xxx();		\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a.b.c.X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    new <CompleteOnType:a.b.c.X>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"a.b.c.Xxx",
		// test name
		"<complete on qualified type reference (shrink none)>"
	);
}
/*
 * Completion on a qualified type reference, where the cursor is right after the
 * last dot.
 */
public void testQualifiedTypeReferenceShrinkOne() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 0;						\t
					new a.b.c.Xxx();		\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"a.b.c.",
		// expectedCompletionNodeToString:
		"<CompleteOnType:a.b.c.>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    new <CompleteOnType:a.b.c.>();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"a.b.c.",
		// test name
		"<complete on qualified type reference (shrink one)>"
	);
}
/*
 * SwitchBlockStatement ::= SwitchLabels <BlockStatements>
 */
public void testSwitchBlockStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				void foo() {						\t
					int i = 1;						\t
					switch (i) {					\t
						case 1: 					\t
							Xxx o = fred(i);		\t
							break;					\t
						default:					\t
					}								\t
				}									\t
			}										\t
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
			    int i;
			    switch (i) {
			    case 1 :
			        <CompleteOnType:X> o;
			        break;
			    default :
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on switch block statement>"
	);
}
/*
 * Throws ::= 'throws' <ClassTypeList>
 */
public void testThrows() {
	runTestCheckDietParse(
		"""
			class Bar {							\t
				void foo() throws Xxx {				\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"X",
		// expectedCompletionNodeToString:
		"<CompleteOnException:X>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() throws <CompleteOnException:X> {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"X",
		// expectedReplacedSource:
		"Xxx",
		// test name
		"<complete on throws>"
	);
}
}
