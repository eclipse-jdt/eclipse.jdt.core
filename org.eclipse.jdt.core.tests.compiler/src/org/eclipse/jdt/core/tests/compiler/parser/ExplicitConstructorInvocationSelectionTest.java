/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

/**
 * Selection is expected to be wrapped with an explicit constructor invocation.
 */
public class ExplicitConstructorInvocationSelectionTest extends AbstractSelectionTest {
public ExplicitConstructorInvocationSelectionTest(String testName) {
	super(testName);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testNameSuper() {
	runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.super(fred());				\t
					}									\t
				}										\t
			}											\t
			""",
		// selectionStartBehind:
		"Bar.super(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      Bar.super(<SelectOnMessageSend:fred()>);
			    }
			  }
			  static Bar x;
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation name super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Name '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testNameThis() {
	runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar() {						\t
						Bar.this(fred());				\t
					}									\t
				}										\t
			}											\t
			""",
		// selectionStartBehind:
		"Bar.this(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar() {
			      Bar.this(<SelectOnMessageSend:fred()>);
			    }
			  }
			  static Bar x;
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation name this>"
	);
}
/*
 * ExplicitConstructorInvocation ::= Primary '.' 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testPrimarySuper() {
	runTestCheckMethodParse(
		"""
			class Bar {								\t
				static Bar x;							\t
				public class InnerBar {					\t
					InnerBar(Bar x) {					\t
					}									\t
				}										\t
				public class SubInnerBar extends InnerBar {\t
					SubInnerBar(Bar x) {				\t
						primary().super(fred());		\t
					}									\t
				}										\t
			}											\t
			""",
		// selectionStartBehind:
		"super(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		"""
			class Bar {
			  public class InnerBar {
			    InnerBar(Bar x) {
			    }
			  }
			  public class SubInnerBar extends InnerBar {
			    SubInnerBar(Bar x) {
			      primary().super(<SelectOnMessageSend:fred()>);
			    }
			  }
			  static Bar x;
			  <clinit>() {
			  }
			  Bar() {
			  }
			}
			""",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation primary super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'super' '(' <ArgumentListopt> ')' ';'
 */
public void testSuper() {
	runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar() {									\t
					super(fred());						\t
				}										\t
			}											\t
			""",
		// selectionStartBehind:
		"super(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		"""
			class Bar {
			  Bar() {
			    super(<SelectOnMessageSend:fred()>);
			  }
			}
			""",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation super>"
	);
}
/*
 * ExplicitConstructorInvocation ::= 'this' '(' <ArgumentListopt> ')' ';'
 */
public void testThis() {
	runTestCheckMethodParse(
		"""
			class Bar {								\t
				Bar() {									\t
					this(fred());						\t
				}										\t
			}											\t
			""",
		// selectionStartBehind:
		"this(",
		// selectionEndBehind:
		"fred",
		// expectedSelectionNodeToString:
		"<SelectOnMessageSend:fred()>",
		"""
			class Bar {
			  Bar() {
			    this(<SelectOnMessageSend:fred()>);
			  }
			}
			""",
		// expectedSelectionIdentifier:
		"fred",
		// expectedReplacedSource:
		"fred()",
		// testName:
		"<select on explicit constructor invocation this>"
	);
}
}
