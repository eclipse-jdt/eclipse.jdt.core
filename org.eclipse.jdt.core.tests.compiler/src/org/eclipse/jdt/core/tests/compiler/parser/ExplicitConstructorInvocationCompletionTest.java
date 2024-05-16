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
 * Completion is expected to be an ExplicitConstructorInvocation
 * or inside an ExplicitConstructorInvocation
 */
public class ExplicitConstructorInvocationCompletionTest extends AbstractCompletionTest {
public ExplicitConstructorInvocationCompletionTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ExplicitConstructorInvocationCompletionTest.class);
}
/*
 * Completion on a qualified 'super' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= Primary '.' 'super' '(' ArgumentListopt ')' ';'
 */
public void testPrimarySuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar(Bar x) {				\t
						primary().super(1, 2, i);		\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"super(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar(Bar x) {
			      primary().super(1, 2, <CompleteOnName:>, i);
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
		"",
		// test name
		"<complete on explicit constructor invocation primary super>"
	);
}
/*
 * Completion on a qualified 'this' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= Primary '.' 'this' '(' ArgumentListopt ')' ';'
 */
public void testPrimaryThis() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar(Bar x) {				\t
						primary().this(1, 2, i);		\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"this(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar(Bar x) {
			      primary().this(1, 2, <CompleteOnName:>, i);
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
		"",
		// test name
		"<complete on explicit constructor invocation primary this>"
	);
}
/*
 * Completion on a 'super' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= 'super' '(' ArgumentListopt ')' ';'
 */
public void testSuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				Bar() {								\t
					super(1, 2, i);					\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"super(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			    super(1, 2, <CompleteOnName:>, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion on 'super' constructor invocation>"
	);
}
/*
 * Completion on a 'this' constructor invocation.
 *
 * ie. ExplicitConstructorInvocation ::= 'this' '(' ArgumentListopt ')' ';'
 */
public void testThis() {
	this.runTestCheckMethodParse(
		"""
			class Bar {							\t
				Bar() {								\t
					this(1, 2, i);					\t
				}									\t
			}										\t
			""",
		// completeBehind:
		"this(1, 2,",
		// expectedCompletionNodeToString:
		"<CompleteOnName:>",
		"""
			class Bar {
			  Bar() {
			    this(1, 2, <CompleteOnName:>, i);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"",
		// expectedReplacedSource:
		"",
		// test name
		"<completion on 'this' constructor invocation>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperNameSuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.super(fred().xyz);			\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"fred().x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      Bar.super(<CompleteOnMemberAccess:fred().x>);
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
		"xyz",
		// test name
		"<complete on wrapper name super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperNameThis() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.this(fred().xyz);			\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"fred().x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      Bar.this(<CompleteOnMemberAccess:fred().x>);
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
		"xyz",
		// test name
		"<complete on wrapper name this>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Primary '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperPrimarySuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar(Bar x) {				\t
						primary().super(fred().xyz);		\t
					}									\t
				}										\t
			}											\t
			""",
		// completeBehind:
		"fred().x",
		// expectedCompletionNodeToString:
		"<CompleteOnMemberAccess:fred().x>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar(Bar x) {
			      primary().super(<CompleteOnMemberAccess:fred().x>);
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
		"xyz",
		// test name
		"<complete on wrapper primary super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperSuper() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar() {									\t
					super(fred().xyz);					\t
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
			    super(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on wrapper super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testWrapperThis() {
	this.runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar() {									\t
					this(fred().xyz);						\t
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
			    this(<CompleteOnMemberAccess:fred().x>);
			  }
			}
			""",
		// expectedCompletionIdentifier:
		"x",
		// expectedReplacedSource:
		"xyz",
		// test name
		"<complete on wrapper this>"
	);
}
}
