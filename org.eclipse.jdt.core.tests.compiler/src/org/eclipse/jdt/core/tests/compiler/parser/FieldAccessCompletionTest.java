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
 * Completion is expected to be a FieldAccess.
 */
public class FieldAccessCompletionTest extends AbstractCompletionTest {
public FieldAccessCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(FieldAccessCompletionTest.class);
}
/*
 * AdditiveExpression ::= AdditiveExpression '-' <MultiplicativeExpression>
 */
public void testAdditiveExpressionMinus() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return 1 - fred().xyz;					\t
				}											\t
			}												\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (1 - <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on additive expression minus>"
	);
}
/*
 * AdditiveExpression ::= AdditiveExpression '+' <MultiplicativeExpression>
 */
public void testAdditiveExpressionPlus() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return 1 + fred().xyz;					\t
				}											\t
			}												\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (1 + <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on additive expression plus>"
	);
}
/*
 * AndExpression ::= AndExpression '&' <EqualityExpression>
 */
public void testAndExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return isTrue & fred().xyz;				\t
				}											\t
			}												\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (isTrue & <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// expectedReplacedSource:
		"<complete on and expression>"
	);
}
/*
 * ArgumentList ::= ArgumentList ',' <Expression>
 */
public void testArgumentList() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					bizz(1, "2", fred().xyz);			\t
				}										\t
			}											\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    bizz(1, "2", <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on argument list>"
	);
}
/*
 * ArrayAccess ::= Name '[' <Expression> ']'
 */
public void testArrayAccess() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				int foo() {								\t
					return v[fred().xyz];					\t
				}										\t
			}											\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return v[<CompleteOnMemberAccess:fred().x>];
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on array access>"
	);
}
/*
 * ArrayAccess ::= PrimaryNoNewArray '[' <Expression> ']'
 */
public void testArrayAccessPrimaryNoNewArray() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				int foo() {								\t
					return buzz()[fred().xyz];			\t
				}										\t
			}											\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return buzz()[<CompleteOnMemberAccess:fred().x>];
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on array access primary no new array>"
	);
}
/*
 * ArrayInitializer ::= '{' <VariableInitializers> '}'
 */
public void testArrayInitializer() {
	this.runTestCheckMethodParse(
		"""
			class Bar {										\t
				void foo() {									\t
					int[] i = new int[] {fred().xyz}			\t
				}												\t
			}													\t
			""",
			// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int[] i = new int[]{<CompleteOnMemberAccess:fred().x>};
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on array initializer>"
	);
}
/*
 * ArrayInitializer ::= '{' <VariableInitializers> , '}'
 */
public void testArrayInitializerComma() {
	this.runTestCheckMethodParse(
		"""
			class Bar {										\t
				void foo() {									\t
					int[] i = new int[] {fred().xyz,}				\t
				}												\t
			}													\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int[] i = new int[]{<CompleteOnMemberAccess:fred().x>};
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on array initializer comma>"
	);
}
/*
 * Assignment ::= LeftHandSide AssignmentOperator <AssignmentExpression>
 */
public void testAssignment() {
	this.runTestCheckMethodParse(
		"""
			class Bar {										\t
				void foo() {									\t
					i = fred().xyz;								\t
				}												\t
			}													\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    i = <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on assignment>"
	);
}
/*
 * Block ::= OpenBlock '{' <BlockStatementsopt> '}'
 */
public void testBlock() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					try {								\t
						fred().xyz = new Foo();			\t
					} catch (Exception e) {}			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    {
			      <CompleteOnMemberAccess:fred().x> = new Foo();
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
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
			class Bar {								\t
				void foo() {							\t
					int i = 0;							\t
					fred().xyz = new Foo();				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on block statements>"
	);
}
/*
 * ConstructorBody ::= NestedMethod '{' ExplicitConstructorInvocation <BlockStatements> '}'
 */
public void testBlockStatementsInConstructorBody() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar() {									\t
					super();								\t
					fred().xyz = new Foo();	\t
				}												\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			    super();
			    <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on block statements in constructor body>"
	);
}
/*
 * BlockStatements ::= BlockStatements <BlockStatement>
 *
 * in a non static initializer.
 */
public void testBlockStatementsInInitializer() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				{										\t
					int i = 0;							\t
					fred().xyz = new Foo();				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  {
			    int i;
			    <CompleteOnMemberAccess:fred().x>;
			  }
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on block statements in initializer>"
	);
}
/*
 * BlockStatements ::= BlockStatements <BlockStatement>
 *
 * in a static initializer.
 */
public void testBlockStatementsInStaticInitializer() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static {								\t
					int i = 0;							\t
					fred().xyz = new Foo();				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  static {
			    int i;
			    <CompleteOnMemberAccess:fred().x>;
			  }
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on block statements in static initializer>"
	);
}
/*
 * CastExpression ::= PushLPAREN <Expression> PushRPAREN UnaryExpressionNotPlusMinus
 *
 * NB: Valid syntaxically but not semantically
 */
public void testCastExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar foo() {								\t
					return (fred().xyz)buzz();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  Bar foo() {
			    return <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on cast expression>"
	);
}
/*
 * CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN <UnaryExpression>
 * or
 * CastExpression ::= PushLPAREN Name Dims PushRPAREN <UnaryExpressionNotPlusMinus>
 * or
 * CastExpression ::= PushLPAREN Expression PushRPAREN <UnaryExpressionNotPlusMinus>
 */
public void testCastExpressionUnaryExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar foo() {								\t
					return (Bar)(fred().xyz);			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  Bar foo() {
			    return (Bar) <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on cast expression unary expression>"
	);
}
/*
 * ClassInstanceCreationExpression ::= 'new' ClassType '(' <ArgumentListopt> ')' ClassBodyopt
 */
public void testClassInstanceCreationExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					new Bar(fred().xyz);					\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    new Bar(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on class instance creation expression>"
	);
}
/*
 * ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionName() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					Bar.new Bar(fred().xyz);				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    Bar.new Bar(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on class instance creation expression name>"
	);
}
/*
 * ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' <ArgumentListopt> ')' ClassBodyopt
 */
public void testClassInstanceCreationExpressionPrimary() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					bizz().new Bar(fred().xyz);			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    bizz().new Bar(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on class instance creation expression primary>"
	);
}
/*
 * ConditionalAndExpression ::= ConditionalAndExpression '&&' <InclusiveOrExpression>
 */
public void testConditionalAndExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return isTrue && fred().xyz;				\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (isTrue && <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on conditional and expression>"
	);
}
/*
 * ConditionalExpression ::= ConditionalOrExpression '?' <Expression> ':' ConditionalExpression
 */
public void testConditionalExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				Bar foo() {									\t
					return fred().xyz == null ? null : new Bar();\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  Bar foo() {
			    return ((<CompleteOnMemberAccess:fred().x> == null) ? null : new Bar());
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on conditional expression>"
	);
}
/*
 * ConditionalExpression ::= ConditionalOrExpression '?' Expression ':' <ConditionalExpression>
 */
public void testConditionalExpressionConditionalExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return isTrue ? true : fred().xyz;		\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (isTrue ? true : <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on conditional expression conditional expression>"
	);
}
/*
 * ConditionalOrExpression ::= ConditionalOrExpression '||' <ConditionalAndExpression>
 */
public void testConditionalOrExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return isTrue || fred().xyz;				\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (isTrue || <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on conditional or expression>"
	);
}
/*
 * ConstructorBody ::= NestedMethod '{' <BlockStatementsopt> '}'
 */
public void testConstructorBody() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar() {									\t
					fred().xyz = new Foo();				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			    super();
			    <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on constructor body>"
	);
}
/*
 * DimWithOrWithOutExpr ::= '[' <Expression> ']'
 */
public void testDimWithOrWithOutExpr() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					int[] v = new int[fred().xyz];		\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int[] v = new int[<CompleteOnMemberAccess:fred().x>];
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on dim with or without expr>"
	);
}
/*
 * DoStatement ::= 'do' Statement 'while' '(' <Expression> ')' ';'
 */
public void testDoExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					do									\t
						System.out.println();			\t
					while (fred().xyz);					\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    do
			      System.out.println();
			while (<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on do expression>"
	);
}
/*
 * DoStatement ::= 'do' <Statement> 'while' '(' Expression ')' ';'
 */
public void testDoStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					do									\t
						fred().xyz = new Foo();			\t
					while (true);						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on do statement>"
	);
}
/*
 * EqualityExpression ::= EqualityExpression '==' <RelationalExpression>
 */
public void testEqualityExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return 1 == fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (1 == <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on equality expression>"
	);
}
/*
 * EqualityExpression ::= EqualityExpression '!=' <RelationalExpression>
 */
public void testEqualityExpressionNot() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return 1 != fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (1 != <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on equality expression not>"
	);
}
/*
 * ExclusiveOrExpression ::= ExclusiveOrExpression '^' <AndExpression>
 */
public void testExclusiveOrExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return isTrue ^ fred().xyz;				\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (isTrue ^ <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on exclusive or expression>"
	);
}
/*
 * ConstructorBody ::= NestedMethod '{' <ExplicitConstructorInvocation> '}'
 * or
 * ConstructorBody ::= NestedMethod '{' <ExplicitConstructorInvocation> BlockStatements '}'
 */
public void testExplicitConstructorInvocationInConstructorBody() {
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
		"<complete on explicit constructor invocation in constructor body>"
	);
}
/*
 * ForStatement ::= 'for' '(' <ForInitopt> ';' Expressionopt ';' ForUpdateopt ')' Statement
 * or
 * ForStatementNoShortIf ::= 'for' '(' <ForInitopt> ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf
 */
public void testForInit() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					for (int i = fred().xyz; i < 2; i++)	\t
						System.out.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i = <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on for init>"
	);
}
/*
 * ForStatement ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' <Statement>
 * or
 * ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' <StatementNoShortIf>
 */
public void testForStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					for (int i = 0; i < 2; i++)			\t
						fred().xyz = new Foo();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    for (int i;; (i < 2); i ++)\s
			      <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on for statement>"
	);
}
/*
 * ForStatement ::= 'for' '(' ForInitopt ';' <Expressionopt> ';' ForUpdateopt ')' Statement
 * or
 * ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' <Expressionopt> ';' ForUpdateopt ')' StatementNoShortIf
 */
public void testForStatementExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					for (int i = 0; fred().xyz > i; i++)	\t
						Systemout.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    for (int i;; (<CompleteOnMemberAccess:fred().x> > i); i ++)\s
			      Systemout.println();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on for statement expression>"
	);
}
/*
 * ForStatement ::= 'for' '(' ForInitopt ';' Expressionopt ';' <ForUpdateopt> ')' Statement
 * or
 * ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' Expressionopt ';' <ForUpdateopt> ')' StatementNoShortIf
 */
public void testForUpdate() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					for (int i = 0; i < 2; i+= fred().xyz)\t
						System.out.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    for (int i;; (i < 2); i += <CompleteOnMemberAccess:fred().x>)\s
			      System.out.println();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on for update>"
	);
}
/*
 * IfThenStatement ::= 'if' '(' <Expression> ')' Statement
 */
public void testIfExpresionThen() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					if (fred().xyz)						\t
						System.out.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    if (<CompleteOnMemberAccess:fred().x>)
			        System.out.println();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on \"if expression then\">"
	);
}
/*
 * IfThenElseStatement ::= 'if' '(' <Expression> ')' StatementNoShortIf 'else' Statement
 * or
 * IfThenElseStatementNoShortIf ::= 'if' '(' <Expression> ')' StatementNoShortIf 'else' StatementNoShortIf
 */
public void testIfExpresionThenElse() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					if (fred().xyz)						\t
						System.out.println();			\t
					else								\t
						System.out.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    if (<CompleteOnMemberAccess:fred().x>)
			        System.out.println();
			    else
			        System.out.println();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on \"if expression then else\">"
	);
}
/*
 * IfThenElseStatement ::= 'if' '(' Expression ')' StatementNoShortIf 'else' <Statement>
 * or
 * IfThenElseStatementNoShortIf ::= 'if' '(' Expression ')' StatementNoShortIf 'else' <StatementNoShortIf>
 */
public void testIfThenElseStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					if (false)							\t
						System.out.println();			\t
					else								\t
						fred().xyz = new Foo();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    if (false)
			        System.out.println();
			    else
			        <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on \"if then else\" statement>"
	);
}
/*
 * IfThenStatement ::= 'if' '(' Expression ')' <Statement>
 */
public void testIfThenStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					if (true)							\t
						fred().xyz = new Foo();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    if (true)
			        <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on \"if then\" statement>"
	);
}
/*
 * IfThenStatementElse ::= 'if' '(' Expression ')' <StatementNoShortIf> 'else' Statement
 * or
 * IfThenElseStatementNoShortIf ::= 'if' '(' Expression ')' <StatementNoShortIf> 'else' StatementNoShortIf
 */
public void testIfThenStatementElse() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					if (true)							\t
						fred().xyz = new Foo();			\t
					else								\t
						System.out.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    if (true)
			        <CompleteOnMemberAccess:fred().x> = new Foo();
			    else
			        System.out.println();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on \"if then statement else\">"
	);
}
/*
 * InclusiveOrExpression ::= InclusiveOrExpression '|' <ExclusiveOrExpression>
 */
public void testInclusiveOrExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return isTrue | fred().xyz;				\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (isTrue | <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on inclusive or expression>"
	);
}
/*
 * LabeledStatement ::= 'Identifier' ':' <Statement>
 * or
 * LabeledStatementNoShortIf ::= 'Identifier' ':' <StatementNoShortIf>
 */
public void testLabeledStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					fredCall: fred().xyz = new Foo();		\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    fredCall: <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// expectedLabels:
		new String[] {"fredCall"},
		// test name
		"<complete on labeled statement>"
	);
}
/*
 * MethodBody ::= NestedMethod '{' <BlockStatementsopt> '}'
 */
public void testMethodBody() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					fred().xyz = new Foo();				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on method body>"
	);
}
/*
 * MethodInvocation ::= Name '(' <ArgumentListopt> ')'
 */
public void testMethodInvocation() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					bizz(fred().xyz);						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    bizz(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on method invocation>"
	);
}
/*
 * MethodInvocation ::= Primary '.' 'Identifier' '(' <ArgumentListopt> ')'
 */
public void testMethodInvocationPrimary() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					buzz().bizz(fred().xyz);				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    buzz().bizz(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on method invocation primary>"
	);
}
/*
 * MethodInvocation ::= 'super' '.' 'Identifier' '(' <ArgumentListopt> ')'
 */
public void testMethodInvocationSuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					super.bizz(fred().xyz);				\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    super.bizz(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on method invocation super>"
	);
}
/*
 * MultiplicativeExpression ::= MultiplicativeExpression '/' <UnaryExpression>
 */
public void testMultiplicativeExpressiondDivision() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				double foo() {								\t
					return 2 / fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  double foo() {
			    return (2 / <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on multiplicative expression division>"
	);
}
/*
 * MultiplicativeExpression ::= MultiplicativeExpression '*' <UnaryExpression>
 */
public void testMultiplicativeExpressionMultiplication() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return 2 * fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (2 * <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on multiplicative expression multiplication>"
	);
}
/*
 * MultiplicativeExpression ::= MultiplicativeExpression '%' <UnaryExpression>
 */
public void testMultiplicativeExpressionRemainder() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return 2 % fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (2 % <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on multiplicative expression remainder>"
	);
}
/*
 * PreDecrementExpression ::= '--' PushPosition <UnaryExpression>
 */
public void testPreIncrementExpressionMinusMinus() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					-- fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    -- <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on pre increment expression minus minus>"
	);
}
/*
 * PreIncrementExpression ::= '++' PushPosition <UnaryExpression>
 */
public void testPreIncrementExpressionPlusPlus() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					++ fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    ++ <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on pre increment expression plus plus>"
	);
}
/*
 * PrimaryNoNewArray ::= PushLPAREN <Expression> PushRPAREN
 */
public void testPrimaryNoNewArray() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					(fred().xyz).zzz();					\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    <CompleteOnMemberAccess:fred().x>.zzz();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on primary no new array>"
	);
}
/*
 * RelationalExpression ::= RelationalExpression '>' <ShiftExpression>
 */
public void testRelationalExpressionGreaterThan() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return 1 > fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (1 > <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on relational expression greater than>"
	);
}
/*
 * RelationalExpression ::= RelationalExpression '>=' <ShiftExpression>
 */
public void testRelationalExpressionGreaterThanOrEquals() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return 1 >= fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (1 >= <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on relational expression greater than or equal>"
	);
}
/*
 * RelationalExpression ::= RelationalExpression '<' <ShiftExpression>
 */
public void testRelationalExpressionLessThan() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return 1 < fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (1 < <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on relational expression less than>"
	);
}
/*
 * RelationalExpression ::= RelationalExpression '<=' <ShiftExpression>
 */
public void testRelationalExpressionLessThanOrEqual() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				boolean foo() {								\t
					return 1 <= fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  boolean foo() {
			    return (1 <= <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on relational expression less than or equal>"
	);
}
/*
 * ReturnStatement ::= 'return' <Expressionopt> ';
 */
public void testReturnStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				int foo() {								\t
					return fred().xyz;					\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on return statement>"
	);
}
/*
 * ShiftExpression ::= ShiftExpression '<<' <AdditiveExpression>
 */
public void testShiftExpressionLeft() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return i << fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (i << <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on shift expression left>"
	);
}
/*
 * ShiftExpression ::= ShiftExpression '>>' <AdditiveExpression>
 */
public void testShiftExpressionRight() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return i >> fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (i >> <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on shift expression right>"
	);
}
/*
 * ShiftExpression ::= ShiftExpression '>>>' <AdditiveExpression>
 */
public void testShiftExpressionRightUnSigned() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				int foo() {									\t
					return i >>> fred().xyz;					\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  int foo() {
			    return (i >>> <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on shift expression right unsigned>"
	);
}
/*
 * StatementExpressionList ::= StatementExpressionList ',' <StatementExpression>
 */
public void testStatementExpressionList() {
	this.runTestCheckMethodParse(
		"""
			class Bar {										\t
				void foo() {									\t
					for (int i = 0, length = fred().xyz; i < 2; i++)\t
						System.out.println();					\t
				}												\t
			}													\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    int length = <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on statement expression list>"
	);
}
/*
 * SwitchBlockStatement ::= SwitchLabels <BlockStatements>
 */
public void testSwitchBlockStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					int i =  0;							\t
					switch (i) {						\t
						case 0: fred().xyz = new Foo();	\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    switch (i) {
			    case 0 :
			        <CompleteOnMemberAccess:fred().x> = new Foo();
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on switch block statement>"
	);
}
/*
 * SwitchStatement ::= 'switch' OpenBlock '(' <Expression> ')' SwitchBlock
 */
public void testSwitchExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					switch (fred().xyz) {					\t
						case 0: System.out.println();	\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    switch (<CompleteOnMemberAccess:fred().x>) {
			    case 0 :
			        System.out.println();
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on switch expression>"
	);
}
/*
 * SwitchLabel ::= 'case' <ConstantExpression> ':'
 */
public void testSwitchLabel() {
	this.runTestCheckMethodParse(
		"""
			class Bar {									\t
				void foo() {								\t
					int i =  0;								\t
					switch (i) {							\t
						case fred().xyz: System.out.println();\t
					}										\t
				}											\t
			}												\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    switch (i) {
			    case <CompleteOnMemberAccess:fred().x> :
			        System.out.println();
			    }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on switch label>"
	);
}
/*
 * SynchronizedStatement ::= OnlySynchronized '(' <Expression> ')' Block
 */
public void testSynchronizedStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					synchronized (fred().xyz) {			\t
						 System.out.println();			\t
					} 									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    synchronized (<CompleteOnMemberAccess:fred().x>)
			      {
			        System.out.println();
			      }
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on synchronized expression>"
	);
}
/*
 * ThrowStatement ::= 'throw' <Expression> ';'
 */
public void testThrowExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					throw fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    throw <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on throw expression>"
	);
}
/*
 * UnaryExpressionNotPlusMinus ::= '~' PushPosition <UnaryExpression>
 */
public void testUnaryExpressionBitwiseComplement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					i = ~ fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    i = (~ <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on unary expression bitwise complement>"
	);
}
/*
 * UnaryExpressionNotPlusMinus ::= '!' PushPosition <UnaryExpression>
 */
public void testUnaryExpressionLogicalComplement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					i = ! fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    i = (! <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on unary expression logical complement>"
	);
}
/*
 * UnaryExpression ::= '-' PushPosition <UnaryExpression>
 */
public void testUnaryExpressionMinus() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					i = - fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    i = (- <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on unary expression minus>"
	);
}
/*
 * UnaryExpression ::= '+' PushPosition <UnaryExpression>
 */
public void testUnaryExpressionPlus() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					i = + fred().xyz;						\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    i = (+ <CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on unary expression plus>"
	);
}
/*
 * VariableDeclarator ::= VariableDeclaratorId EnterField '=' ForceNoDiet <VariableInitializer> RestoreDiet ExitField
 */
public void testVariableDeclarator() {
	this.runTestCheckMethodParse(
		"""
			class Bar {										\t
				void foo() {									\t
					int i = fred().xyz;							\t
				}												\t
			}													\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i = <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on variable declarator>"
	);
}
/*
 * VariableInitializers ::= VariableInitializers ',' <VariableInitializer>
 */
public void testVariableInitializers() {
	this.runTestCheckMethodParse(
		"""
			class Bar {										\t
				void foo() {									\t
					int i = 0, j = fred().xyz;					\t
				}												\t
			}													\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    int i;
			    int j = <CompleteOnMemberAccess:fred().x>;
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on variable initializers>"
	);
}
/*
 * WhileStatement ::= 'while' '(' <Expression> ')' Statement
 * or
 * WhileStatementNoShortIf ::= 'while' '(' <Expression> ')' StatementNoShortIf
 */
public void testWhileExpression() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					while (fred().xyz)					\t
						System.out.println();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    while (<CompleteOnMemberAccess:fred().x>)      System.out.println();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on while expresion>"
	);
}
/*
 * WhileStatement ::= 'while' '(' Expression ')' <Statement>
 * or
 * WhileStatementNoShortIf ::= 'while' '(' Expression ')' <StatementNoShortIf>
 */
public void testWhileStatement() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				void foo() {							\t
					while (true)						\t
						fred().xyz = new Foo();			\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  Bar() {
			  }
			  void foo() {
			    while (true)      <CompleteOnMemberAccess:fred().x> = new Foo();
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on while statement>"
	);
}
}
